package durbin.samplepsychic

import java.security.SecureRandom
import com.vaadin.server.VaadinServlet;
import grapnel.util.SessionUtils as SU

class SamplePsychicSession {
			
	/**
	 * Returns the full path to a subdirectory of the servlet directory. 
	 * @param subdir
	 * @return
	 */
	static String getSubdirPath(subdir){
		String fullPath = VaadinServlet.getCurrent().getServletContext().getRealPath(subdir);
		return(fullPath)
	}
		
	/**
	 * Takes a session token and generates a file name from it.  
	 * @param token
	 * @return
	 */
	static String getResultsFileNameFromToken(token,jobDir){				
		def prefix = "samplePsychic_"
		return(SU.getResultsFileNameFromToken(token,jobDir,prefix))
	}
	
	static def generateResultsTokenAndFileName(jobDir){
		def prefix = "samplePsychic_"
		return(SU.generateResultsTokenAndFileName(jobDir,prefix))
	}		
}


