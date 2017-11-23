package durbin.samplepsychic

import java.util.ArrayList;

import grapnel.util.DoubleTable;
import grapnel.weka.*;
import weka.core.*;
import weka.filters.*;
import swiftml.*;

import javax.servlet.ServletContext

import weka.core.*

class ClassifierCompendium {
	
	static final long serialVersionUID = 1L;
	
	def modelList = []
	def modelName2Model = [:]
	
	
	def ClassifierCompendium(modelDir){
		readModels(modelDir)
	}
	
	public def readModels(modelDir){
		System.err.println("*** LOADING LOTS OF CLASSIFIER MODELS *******")
				
		new File(modelDir).eachFileMatch(~/.*wmm.*/){modelfile->
			try{
				System.err.print("Reading "+modelfile+" ...")
				def model = (WekaMineModel) weka.core.SerializationHelper.read(modelfile.canonicalPath);
				modelList<<model
				modelName2Model[model.className] = model // save so can look up model from results list
				System.err.println("done")
			}catch(Exception e){
				System.err.println "EXCEPTION: \n"+e
			}
		}
		System.err.println("********************* DONE ******************")
	}
	
	public def applyModels(expressionData){
		def allModelResults = []
		
		modelList.each{model->
			System.err.println "Applying ${model.className} to ${expressionData.numInstances()} samples."
			def results = applyModel(expressionData,model)
			System.err.println "\tDone ${results.size()} classifications returned. "
			allModelResults.addAll(results)
		}
		return(allModelResults);
	}
	
	public def applyModel(Instances instances,WekaMineModel model){
				
		/*
		 * Going to assume all models exponentially normalized up front and perform Exponential normalization
		 * once on uploaded data.
		if (model.filter == null){
			System.err.println "No pre-processing filter specified in model."
		}else{
			System.err.println "Applying filter: "+model.filter.class
			instances = WekaMine.applyUnsupervisedFilter(instances,model.filter)
		}
		*/
		
		// The model has no ID, so we save the ID for later reporting...
		def instanceIDs = instances.attributeValues("ID") as ArrayList
		
		instances = InstanceUtils.createInstancesToMatchAttributeList(instances,model.attributes)
		instances = WekaMine.createEmptyClassAttribute(instances,model.className,model.classValues)
		instances.setClassName(model.className)
		
		ArrayList<Classification> results = model.classify(instances)
		
		// Add the background confidence...
		def resultsPlus = []
		results.eachWithIndex{result,i->
			//System.err.println "adding classification plus for instance ${instanceIDs[i]}"
			
			def nullConf0,nullConf1
			if (model.bnm != null){
				def prForValues = result.prForValues as ArrayList // ??
				nullConf0 = model.bnm.getSignificance(prForValues[0],0)
				nullConf1 = model.bnm.getSignificance(prForValues[1],1)
			}
			
			resultsPlus << new ClassificationPlus(instanceIDs[i],model.className,result,nullConf0,nullConf1)
		}
				
		return(resultsPlus)
	}
	
	def normalize(instances){
		WekaAdditions.enable()
		
		System.err.print "Applying exponential normalization filter..."
		def instNames = instances.attributeValues("ID")
		def noIDinstances = AttributeUtils.removeInstanceID(instances)
							
		// Apply the attribute selection algorithm to instances...
		def filter = new ExponentialNormalizationFilter()
		filter.setInputFormat(noIDinstances);
		noIDinstances = Filter.useFilter(noIDinstances,filter)
							
		instances = WekaMine.addID(noIDinstances,instNames)
		System.err.println "done."
		return(instances)
	}			
	
	def getResultsBySample(results){
		def sample2ResultList = [:]
		results.each{r->
				if (!sample2ResultList.containsKey(r.sampleID)){
					sample2ResultList[r.sampleID] = []
				}
				sample2ResultList[r.sampleID]<<r
		}
		return(sample2ResultList)
	}
	
	// KJD TODO:  Make sure that all of the cutoffs are using the same value, nullConf 
	// or classifier score. 
	def getBestResults(results,minScore){
		def bestResults = []
		results.each{r->
			//System.err.println "${r.nullConf0}"
			if (r.nullConf0 >= minScore){
				bestResults<<r
			}
		}
		return(bestResults)
	}
	
	public def each(Closure closure) {
		modelList.each{
			closure.call(it)
		}
	}
	
	/*
	def getCallValue(r){
		def maxIdx = getMaxIdx(r.prForValues)
		def nullConf
		if (maxIdx == 0) nullConf = r.nullConf0
		else nullConf = r.nullConf1
		def call = r.classValues[maxIdx] // look up the name of this.
		if (call == "gt") call = "Greater than median IC50"
		else call = "Less than median IC50"
		return(call)
	}
	*/
	
	def getMaxIdx(list){
		def maxVal = -9999999;
		def maxIdx = 0;
		list.eachWithIndex{val,i->
			if (val > maxVal) {
				maxVal = val
				maxIdx = i
			}
		}
		return(maxIdx)
	}
	
	def saveResults(resultsFile,results){
		weka.core.SerializationHelper.write(resultsFile,results);		
	}
		
	ArrayList<ClassificationPlus> loadResults(resultsFile){
		def savedResults = (ArrayList<ClassificationPlus>) weka.core.SerializationHelper.read(resultsFile);
		return(savedResults)
	}
	
}
