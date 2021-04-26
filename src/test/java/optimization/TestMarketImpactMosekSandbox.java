package optimization;


import mosek.fusion.*;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import config.CoreConfig;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CoreConfig.class)
public class TestMarketImpactMosekSandbox {

	@Test
	public void testMosek()  {

	}

	public static double sum(double[] x) {
	    double r = 0.0;
	    for (int i = 0; i < x.length; ++i) r += x[i];
	    return r;
	  }

	  public static double dot(double[] x, double[] y) {
	    double r = 0.0;
	    for (int i = 0; i < x.length; ++i) r += x[i] * y[i];
	    return r;
	  }
	  
	

	  /*
	      Description:
	          Extends the basic Markowitz model with a market cost term.

	      Input:
	          n: Number of assets
	          mu: An n dimmensional vector of expected returns
	          GT: A matrix with n columns so (GT')*GT  = covariance matrix'
	          x0: Initial holdings
	          w: Initial cash holding
	          gamma: Maximum risk (=std. dev) accepted
	          m: It is assumed that  market impact cost for the j'th asset is
	             m_j|x_j-x0_j|^3/2

	      Output:
	         Optimal expected return and the optimal portfolio

	  */
	  public static void MarkowitzWithMarketImpact
	  ( int n,
	    double[] mu,
	    double[][] GT,
	    double[] x0,
	    double   w,
	    double   gamma,
	    double[] m,
	    double[] xsol,
	    double[] tsol)
	  throws mosek.fusion.SolutionError {
	    Model M = new Model("Markowitz portfolio with market impact");
	    try {
	      //M.setLogHandler(new java.io.PrintWriter(System.out));

	      //Variable x = M.variable("x", n, Domain.greaterThan(0.0));
	      Variable x = M.variable("x", n, Domain.inRange(-1.0, 1.0));	      	      
	      
	      // Variables computing market impact
	      Variable t = M.variable("t", n, Domain.unbounded());
	     
	      // Gross delta constraint
	      Variable l = M.variable("long", n, Domain.greaterThan(0.0));	  
	      M.constraint(Expr.sub(l,x), Domain.greaterThan(0.0));	      
	      Variable s = M.variable("short", n, Domain.lessThan(0.0));	  
	      M.constraint(Expr.sub(s,x), Domain.lessThan(0.0));
	      M.constraint("longBound", Expr.sum(l),Domain.lessThan(3.0));
	      M.constraint("shortBound", Expr.sum(s),Domain.greaterThan(-3.0));
	      	      
	      // Net delta constraint
	      M.constraint("netdeltalow", Expr.sum(x),Domain.greaterThan(-0.3));
	      M.constraint("netdeltahigh", Expr.sum(x),Domain.lessThan(0.3));
	      
	      //  Maximize expected return minus impact
	      M.objective("obj", ObjectiveSense.Maximize, Expr.sub(Expr.dot(mu, x),Expr.dot(m, t)));

	      // Invested amount + slippage cost = initial wealth
	      //M.constraint("budget", Expr.add(Expr.sum(x), Expr.dot(m, t)), Domain.equalsTo(w + sum(x0)));

	      // Imposes a bound on the risk
	      M.constraint("risk", Expr.vstack(gamma, Expr.mul(GT, x)), Domain.inQCone());

	      // t >= |x-x0|^1.5 using a power cone
	      M.constraint("tz", Expr.hstack(t, Expr.constTerm(n, 1.0), Expr.sub(x,x0)), Domain.inPPowerCone(2.0/3.0));

	      M.solve();

	      if (xsol != null)
	        System.arraycopy(x.level(), 0, xsol, 0, n);
	      if (tsol != null)
	        System.arraycopy(t.level(), 0, tsol, 0, n);
	    } finally {
	      M.dispose();
	      System.out.println(Arrays.toString(xsol));
	    }
	  }


	  /*
	    The example. Reads in data and solves the portfolio models.
	   */
	  public static void main(String[] argv)
	  throws java.io.IOException,
	         java.io.FileNotFoundException,
	         mosek.fusion.SolutionError {

	    int        n      = 3;
	    double     w      = 1.0;
	    double[]   mu     = {-0.1073, -0.1737, 0.0627};
	    double[]   x0     = {0.0, 0.0, 0.0};
	    double[]   gammas = {0.035, 0.040, 0.050, 0.060, 0.070, 0.080, 0.390};
	    double[][] GT     = {
	      { 0.166673333200005, 0.0232190712557243 ,  0.0012599496030238 },
	      { 0.0              , 0.102863378954911  , -0.00222873156550421},
	      { 0.0              , 0.0                ,  0.0338148677744977 }
	    };

	    double[] m = {  1.0e-2, 1.0e-2, 1.0e-2};
	    // Somewhat arbirtrary choice of m
	    //double[] m = new double[n]; for (int i = 0; i < n; ++i) m[i] = 1.0e-2;
	    double[] x = new double[n];
	    double[] t = new double[n];
	    double gamma = gammas[6];

	    MarkowitzWithMarketImpact(n, mu, GT, x0, w, gamma, m, x, t);
	    System.out.println("\n-----------------------------------------------------------------------------------");
	    System.out.println("Markowitz portfolio optimization with market impact cost");
	    System.out.println("-----------------------------------------------------------------------------------\n");
	    System.out.format("Expected return: %.4e Std. deviation: %.4e Market impact cost: %.4e\n",
	                      dot(mu, x),
	                      gamma,
	                      dot(m, t));
	    System.out.println("-----------------------------------------------------------------------------------\n");

	  }
}
