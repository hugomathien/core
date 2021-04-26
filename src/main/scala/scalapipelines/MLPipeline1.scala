package scalapipelines
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.Row
import java.time.Instant
import javapipelines.IDatasetTransformer;
import org.apache.spark.ml.feature._
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.ml.{Pipeline, PipelineModel}
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.expressions.WindowSpec
import org.apache.spark.sql.expressions.UserDefinedFunction

import scalaudaf.Ema
import org.apache.spark.ml.regression.{LinearRegression, LinearRegressionModel}
import org.apache.spark.ml.regression.{RandomForestRegressor, RandomForestRegressionModel}
import org.apache.spark.ml.classification.{RandomForestClassificationModel, RandomForestClassifier}
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator

class MLPipeline1 extends IDatasetTransformer {
	def compute(spark: SparkSession,df: Dataset[Row], t: Instant): Dataset[Row] = { 

			// Register UDAF
			//spark.udf.register("ema", udaf(Ema))
			    val emalt: UserDefinedFunction = spark.udf.register("emalt",udaf(new Ema(0.03846,26))) // 26 days ema
					val emast: UserDefinedFunction = spark.udf.register("emast",udaf(new Ema(0.08333,12))) // 12 days ema
					val emavst: UserDefinedFunction = spark.udf.register("emavst",udaf(new Ema(0.11111,9))) // 9 days ema

					// windows
					val fullWindow = Window
					.partitionBy("ticker")
					.orderBy("date")


					// create labels
					// 1d,20d returns

					val labelleddf = df
					.withColumn("lag_price_1d", lag("px_last",1).over(fullWindow))
					.withColumn("lag_price_5d", lag("px_last",5).over(fullWindow))
					.withColumn("lag_price_20d", lag("px_last",20).over(fullWindow))
					.withColumn("return_1d_pct", expr("100*(px_last/lag_price_1d-1)"))
					.withColumn("return_5d_pct", expr("100*(px_last/lag_price_5d-1)"))
					.withColumn("return_20d_pct", expr("100*(px_last/lag_price_20d-1)"))
					.withColumn("fwd_return_1d_pct",lead("return_1d_pct",1).over(fullWindow))
					.withColumn("fwd_return_5d_pct",lead("return_5d_pct",5).over(fullWindow))
					.withColumn("fwd_return_20d_pct",lead("return_20d_pct",20).over(fullWindow))

					// create features			  
					// technicals: rsi,high/low,bollinger,stochastic, open vs close, 
					val rsidf = labelleddf
					.withColumn("avgGain_14d",avg(when(expr("return_1d_pct > 0"), col("return_1d_pct"))).over(fullWindow.rowsBetween(-13,0)))
					.withColumn("avgLoss_14d",avg(when(expr("return_1d_pct <= 0"), col("return_1d_pct"))).over(fullWindow.rowsBetween(-13,0)))
					.withColumn("rsi_14d",expr("100-(100/(1+(((avgGain_14d/100)/14)/((-1*avgLoss_14d/100)/14))))"))


					val bollingerdf = rsidf
					.withColumn("avg_price_20d",avg("px_last").over(fullWindow.rowsBetween(-19,0)))
					.withColumn("std_price_20d",stddev("px_last").over(fullWindow.rowsBetween(-19,0)))
					.withColumn("std_price_20d",stddev("px_last").over(fullWindow.rowsBetween(-19,0)))
					.withColumn("px_20davg_ratio_pct",expr("100*(px_last/avg_price_20d-1)"))
					.withColumn("px_bollinger_uband_ratio_pct",expr("100*(px_last/(avg_price_20d+2*std_price_20d)-1)"))
					.withColumn("px_bollinger_lband_ratio_pct",expr("100*(px_last/(avg_price_20d-2*std_price_20d)-1)"))

					val obvdf = bollingerdf
					.withColumn("obv_day",when(expr("return_1d_pct > 0"), expr("volume/1000000"))
							.otherwise(when(expr("return_1d_pct < 0"), expr("-volume/1000000")).otherwise(lit(0))))			            
					.withColumn("obv",sum("obv_day").over(fullWindow.rowsBetween(Window.unboundedPreceding,0)))
					.withColumn("obv_avg_20d",avg("obv").over(fullWindow.rowsBetween(-19,0)))
					.withColumn("obv_20davg_ratio",expr("obv/obv_avg_20d-1"))

					val addf = obvdf
					.withColumn("high_20d",max("high").over(fullWindow.rowsBetween(-19,0)))
					.withColumn("low_20d",min("low").over(fullWindow.rowsBetween(-19,0)))
					.withColumn("mfm_day",expr("((px_last-low_20d)-(high_20d-px_last))/(high_20d-low_20d)"))	 // change high low to longer period		            
					.withColumn("ad",sum(expr("mfm_day*volume/1000000")).over(fullWindow.rowsBetween(Window.unboundedPreceding,0)))
					.withColumn("ad_avg_20d",avg("ad").over(fullWindow.rowsBetween(-19,0)))
					.withColumn("ad_20davg_ratio",expr("ad/ad_avg_20d-1"))

					val macddf = addf
					.withColumn("emalt",emalt.apply(col("px_last")).over(fullWindow.rowsBetween(-25,0)))
					.withColumn("emast",emast.apply(col("px_last")).over(fullWindow.rowsBetween(-11,0)))
					.withColumn("macd",expr("emast-emalt"))
					.withColumn("macdsignal",emavst.apply(col("macd")).over(fullWindow.rowsBetween(-8,0)))
					.withColumn("macd_to_signal_ratio_pct",expr("100*(macd/macdsignal-1)"))

					val newsdf = macddf
					.withColumn("news_sentiment_daily_avg_chg",col("news_sentiment_daily_avg")-lag("news_sentiment_daily_avg",1).over(fullWindow))
					.withColumn("news_sentiment_daily_avg",when(expr("news_sentiment_daily_avg_chg!=0"),col("news_sentiment_daily_avg")).otherwise(lit(0)))

					val volskewdf = newsdf
					.withColumnRenamed("30day_impvol_90.0%mny_df","30day_impvol_90pct")
					.withColumnRenamed("30day_impvol_100.0%mny_df","30day_impvol_100pct")
					.withColumnRenamed("30day_impvol_110.0%mny_df","30day_impvol_110pct")
					.withColumnRenamed("60day_impvol_90.0%mny_df","60day_impvol_90pct")
					.withColumnRenamed("60day_impvol_100.0%mny_df","60day_impvol_100pct")
					.withColumnRenamed("60day_impvol_110.0%mny_df","60day_impvol_110pct")
					.withColumn("volskew30d",expr("(30day_impvol_90pct-30day_impvol_110pct)/30day_impvol_100pct"))
					.withColumn("volskew60d",expr("(30day_impvol_90pct-30day_impvol_110pct)/30day_impvol_100pct"))
					.withColumn("volskew30d_5dchg",col("volskew30d")-lag("volskew30d",5).over(fullWindow))
					.withColumn("volskew30d_20dchg",col("volskew30d")-lag("volskew30d",20).over(fullWindow))
					.withColumn("volskew60d_5dchg",col("volskew60d")-lag("volskew60d",5).over(fullWindow))
					.withColumn("volskew60d_20dchg",col("volskew60d")-lag("volskew60d",20).over(fullWindow))

					val voltermstructuredf = volskewdf
					.withColumn("vol_atm_60d_30d",expr("(60day_impvol_100pct-30day_impvol_100pct)/((30day_impvol_100pct+60day_impvol_100pct)/2)"))
					.withColumn("vol_30d_carry",expr("30day_impvol_100pct-volatility_30d"))
					.withColumn("vol_60d_carry",expr("60day_impvol_100pct-volatility_60d"))


					val analystrecdf = voltermstructuredf
					.withColumn("buyratio",expr("tot_buy_rec/tot_analyst_rec"))
					.withColumn("sellratio",expr("tot_sell_rec/tot_analyst_rec"))
					.withColumn("holdratio",expr("tot_sell_rec/tot_analyst_rec"))
					.withColumn("buyratio_1dchg",col("buyratio")-lag("buyratio",1).over(fullWindow))
					.withColumn("sellratio_1dchg",col("sellratio")-lag("sellratio",1).over(fullWindow))
					.withColumn("holdratio_1dchg",col("holdratio")-lag("holdratio",1).over(fullWindow))
					.withColumn("px_to_target_ratio_pct",expr("100*(px_last/best_target_price_median-1)"))
					.withColumn("px_target_1dchg",lit(100)*(col("best_target_price_median")/lag("best_target_price_median",1).over(fullWindow)-lit(1)))

					val analystepsdf = analystrecdf
					.withColumn("eps1bf_1dchg",(col("best_eps_median_1bf")-lag("best_eps_median_1bf",1).over(fullWindow))/lag("best_eps_median_1bf",1).over(fullWindow))
					.withColumn("eps2bf_1dchg",(col("best_eps_median_2bf")-lag("best_eps_median_2bf",1).over(fullWindow))/lag("best_eps_median_2bf",1).over(fullWindow))
					.withColumn("eps1bf_20dchg",(col("best_eps_median_1bf")-lag("best_eps_median_1bf",20).over(fullWindow))/lag("best_eps_median_1bf",20).over(fullWindow))
					.withColumn("eps2bf_20dchg",(col("best_eps_median_2bf")-lag("best_eps_median_2bf",20).over(fullWindow))/lag("best_eps_median_2bf",20).over(fullWindow))

					val finaldf = analystepsdf										
			    .withColumn("label",when(expr("fwd_return_5d_pct>0"),lit(1)).otherwise(lit(0)))
					.na.fill(0.0, {Array(
					    "label",
							"eps1bf_1dchg", 
							"eps2bf_1dchg", 
							"eps1bf_20dchg",
							"eps2bf_20dchg",
							"px_target_1dchg",							
							"ad_20davg_ratio",
							"obv_20davg_ratio",													
							"volskew30d_5dchg",
							"volskew30d_20dchg",
							"volskew60d_5dchg",
							"volskew60d_20dchg"	,
							"fwd_return_1d_pct",
							"fwd_return_5d_pct",
							"fwd_return_20d_pct",
							"mf_blck_1d",
							"mf_nonblck_1d"
							)})		

					val imputer = new Imputer()
					.setInputCols(Array(
							"px_to_target_ratio_pct",
							"macd_to_signal_ratio_pct",
							"px_bollinger_lband_ratio_pct",
							"px_bollinger_uband_ratio_pct",
							"px_20davg_ratio_pct",
							"rsi_14d",
							"vol_atm_60d_30d",
							"vol_30d_carry",
							"vol_60d_carry",
							"buyratio",
							"sellratio",
							"holdratio"))
					.setOutputCols(Array(
							"px_to_target_ratio_pct",
							"macd_to_signal_ratio_pct",
							"px_bollinger_lband_ratio_pct",
							"px_bollinger_uband_ratio_pct",
							"px_20davg_ratio_pct",
							"rsi_14d",
							"vol_atm_60d_30d",
							"vol_30d_carry",
							"vol_60d_carry",
							"buyratio",
							"sellratio",
							"holdratio"))
					.setStrategy("median")

					val scaler_assembler = new VectorAssembler()
					.setInputCols(Array(
							"eps1bf_1dchg", 
							"eps2bf_1dchg", 			    
							"px_target_1dchg",
							"px_to_target_ratio_pct",
							"macd_to_signal_ratio_pct",
							"px_bollinger_lband_ratio_pct",
							"px_bollinger_uband_ratio_pct",
							"px_20davg_ratio_pct",
							"mf_blck_1d",
							"mf_nonblck_1d"))
					.setOutputCol("features_toscale")
					.setHandleInvalid("skip")

					val scaler = new StandardScaler()
					.setInputCol("features_toscale")
					.setOutputCol("features_scaled")
					.setWithStd(true)
					.setWithMean(true)

					val raw_assembler = new VectorAssembler()
					.setInputCols(Array(
							"rsi_14d",
							"volskew30d",
							"volskew60d",		
							"vol_atm_60d_30d",
							"vol_30d_carry",
							"vol_60d_carry",
							"buyratio",
							"sellratio",
							"holdratio",
							"news_sentiment_daily_avg"))
					.setOutputCol("features_noscale")
					.setHandleInvalid("skip")


					val final_assembler = new VectorAssembler()
					.setInputCols(Array("features_noscale","features_scaled"))
					.setOutputCol("features")
					.setHandleInvalid("skip")

				/*	val lr = new LinearRegression()
  			  .setLabelCol("label")
          .setFeaturesCol("features")
					.setSolver("normal")
					.setElasticNetParam(0.0)					
					.setFitIntercept(false)*/

					
	        // Train a RandomForest model.
          val rf = new RandomForestClassifier()
            .setLabelCol("label")
            .setFeaturesCol("features")
            .setNumTrees(5)
					
					val pipeline = new Pipeline()
					.setStages(Array(imputer,scaler_assembler,scaler,raw_assembler,final_assembler,rf))

					val testdf = finaldf
					val pipelineModel = pipeline.fit(testdf)

					val transformeddf = pipelineModel.transform(testdf)

					transformeddf		
					.select("date","ticker","label","prediction")
					.orderBy("date")
					.show(false)

					/*val lrModel = pipelineModel.stages.last.asInstanceOf[org.apache.spark.ml.regression.LinearRegressionModel]

					val summary = lrModel.summary

					// Print the coefficients and intercept for logistic regression
					println(s"Coefficients: ${lrModel.coefficients} Intercept: ${lrModel.intercept}")
			
					// Summarize the model over the training set and print out some metrics
					val trainingSummary = lrModel.summary
					println(s"RMSE: ${trainingSummary.rootMeanSquaredError}")
					println(s"r2: ${trainingSummary.r2}")
					println(s"T Values: ${summary.tValues.mkString(",")}")
					println(s"P Values: ${summary.pValues.mkString(",")}")*/
					
					// Select (prediction, true label) and compute test error.

          val evaluator = new MulticlassClassificationEvaluator()
            .setLabelCol("label")
            .setPredictionCol("prediction")
            .setMetricName("accuracy")
          val accuracy = evaluator.evaluate(transformeddf)
          println(s"Test Error = ${(1.0 - accuracy)}")
                    
          val rfModel = pipelineModel.stages.last.asInstanceOf[RandomForestClassificationModel]
         // println(s"Learned classification forest model:\n ${rfModel.toDebugString}")
          println(s"Feature importance:\n ${rfModel.featureImportances}")
          transformeddf.groupBy("label","prediction").count().show()
          
        testdf
	}
}
