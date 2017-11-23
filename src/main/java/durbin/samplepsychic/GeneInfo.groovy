package durbin.samplepsychic



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
}