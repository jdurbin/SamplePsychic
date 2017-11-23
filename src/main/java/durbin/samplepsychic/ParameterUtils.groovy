package durbin.samplepsychic

import java.util.Enumeration;

import com.vaadin.server.VaadinService;

class ParameterUtils {
	
	
	// Test function, not used. 
	static processRequest(req){
		//System.err.println "========== NEW REQUEST ============"
		//Enumeration<String> attrNames = req.getAttributeNames();
		//attrNames.each{
		//	System.err.println "attrNames: "+it
		//}
				
		def paramMap = req.getParameterMap()
		//paramMap.each{k,v->
		//	System.err.println "paramMap: "+k+"\t"+v
		//}
		def runID = paramMap['runID']
		System.err.println "runID: "+runID
		
	}
	
	static String getRunID(){
		//	v-loc:[http://localhost:8080/#!results/RP4qAZiI]
		//  Checks to see if URI is pointing to results
		def req = VaadinService.getCurrent().getCurrentRequest()
		def paramMap = req.getParameterMap()
		def vlocList = paramMap['v-loc']
		String runState = null 
		String runID = null
		if (vlocList != null){
			def uri = vlocList[0]
			System.err.println "uri:"+uri
			def split1 = uri.split("#!")
			System.err.println "split1: "+split1
			if (split1.size() > 1){
				def split2 = split1[1].split("/")
				System.err.println "split2: "+split2			
				if (split2.size() > 1){
					runState = split2[0]
					runID = split2[1]
				}
			}			
		}
		return(runID)
	}

	
	/**
	 * Takes the current VaadinRequest and parses out the runID parameter
	 * @param VaadinRequest req
	 * @return
	 */
	static String getRunIDParameter(){
		def req = VaadinService.getCurrent().getCurrentRequest()
		def paramMap = req.getParameterMap()
		System.err.println "DEBUG: paramMap:"+paramMap
		def runIDList = paramMap['runID']
		def runID
		if (runIDList != null){
			runID = runIDList[0]
		}else{
			runID = null
		}
		System.err.println "ParameterUtils.getRunID runID: "+runID
		return(runID);
	}
}
