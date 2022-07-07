package marketdata.field;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import config.CoreConfig;
import finance.misc.TradingPhaseEnum;
import marketdata.services.base.DataServiceEnum;

@Component
@Lazy(false)
public class FieldConfig {
	@Value("${config.fieldmapper}")
	private Resource resourceFile;
	private HashMap<DataServiceEnum,HashMap<String,Field>> serviceFieldMap;
	
	public FieldConfig() {
		this.serviceFieldMap = new HashMap<DataServiceEnum,HashMap<String,Field>>();
	}
	
	@PostConstruct
	private List<String[]> setupData() throws IOException, BeansException, ClassNotFoundException {
		String line = "";
		String csvSplitBy = ",";
		int serviceColumnBegin = 5;
		InputStream file = resourceFile.getInputStream();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(file))) {
			line = br.readLine();
			String[] headers = line.split(csvSplitBy);
			List<DataServiceEnum> dataServices = new ArrayList<DataServiceEnum>();
			for(int i = 5;i<headers.length;i++) {
				dataServices.add(DataServiceEnum.valueOf(headers[i]));
				serviceFieldMap.put(DataServiceEnum.valueOf(headers[i]), new HashMap<String,Field>());				
			}
			
			while((line = br.readLine()) != null) {
				String[] linearr = line.split(csvSplitBy);
				Field field = Field.get(linearr[0]);
				field.setType(Class.forName(linearr[1]));
				field.setIsPriceData(Boolean.valueOf(linearr[2]));
				
				if(!linearr[3].toString().equals("")) {					
					field.setTradingPhaseEnum(TradingPhaseEnum.valueOf(linearr[3].toString()));
					field.setTradingPhaseStart(Boolean.valueOf(linearr[4]));;
				}
				
				for(int i=serviceColumnBegin;i<linearr.length;i++) {
					field.getMapDataServiceToString().put(dataServices.get(i-serviceColumnBegin),linearr[i]);
					serviceFieldMap.get(dataServices.get(i-serviceColumnBegin)).put(linearr[i],field);
				}
			}
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
