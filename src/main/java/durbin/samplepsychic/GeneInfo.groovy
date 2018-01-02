package durbin.samplepsychic

import grapnel.util.*;
import grapnel.weka.*;
import weka.core.*;

class GeneInfo{
		
	static HashMap readGeneDescriptions(genedescriptions){
		System.err.print "Reading gene descriptions..."
		def gene2description = [:]
		new File(genedescriptions).splitEachLine("\t"){fields->
			def gene = fields[0]
			def description = fields[1]
			gene2description[gene] = description
		}			
		System.err.println "done.  ${gene2description.size()}"
	
	//def debugCount = 0;
	//gene2description.each{k,v->
	//if (debugCount > 20) return;
	//	println "DEBUG: $k \t $v"
	//	debugCount++
	//}
	
		return gene2description
	}
	
	static HashMap readENSEMBL2HGNC(ensembl2hgncFile){
		System.err.print "Reading gene mapping..."
		def ensembl2hgnc = [:]
		new OnlineTable(ensembl2hgncFile).eachRow{r->
			ensembl2hgnc[r.ensembl] = r.hgnc
		}				
		System.err.println "done.  ${ensembl2hgnc.size()}"	
		return ensembl2hgnc
	}
	
	
	static boolean isENSEMBL(Instances data){
		boolean bRvalue = false;
		WekaAdditions.enable();
		def attrNames = data.attributeNames()
		attrNames.each{name->
			if (name.startsWith("ENSG")) bRvalue = true;				
		}
		return(bRvalue);		
	}
	
}