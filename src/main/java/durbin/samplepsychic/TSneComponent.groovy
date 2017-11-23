package durbin.samplepsychic

import com.vaadin.ui.Grid
import com.vaadin.ui.TextField
import com.vaadin.ui.VerticalLayout
import com.vaadin.shared.ui.grid.HeightMode
import com.vaadin.ui.Notification
import com.vaadin.ui.Grid.HeaderRow
import com.vaadin.ui.Grid.HeaderCell
import com.vaadin.data.Container.Filterable
import com.vaadin.data.util.IndexedContainer
import com.vaadin.data.util.filter.SimpleStringFilter
import com.vaadin.data.Item
import com.vaadin.event.FieldEvents.TextChangeEvent
import com.vaadin.event.FieldEvents.TextChangeListener
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.ui.themes.ValoTheme.*;
import com.vaadin.server.Sizeable.Unit.*
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.data.util.filter.Compare
import com.vaadin.data.util.filter.Compare.*;
import com.vaadin.data.Container.*
import durbin.util.*
import com.jujutsu.tsne.FastTSne;
import com.jujutsu.tsne.MatrixOps;
import com.jujutsu.tsne.TSne;

import grapnel.util.*


/**
public class ClassificationPlus{
	def sampleID
	def prForValues = []
	def nullConf0
	def nullConf1
	def nullForValues = []
	def classValues = []
	def nompred
	def modelName
	
	def prForName(name){
		classValues.eachWithIndex{cname,i->
			if (name == cname) return(prForValues[i])
		}
		System.err.println "WARNING: prForName: $name not found."
		return(-1);
	}
}
*/

class TsneComponent extends VerticalLayout{
	SamplePsychicUI app;
	HeaderRow filterRow;
	
	def TsneComponent(SamplePsychicUI vapp){
		app = vapp			
	}
	
	def getIDAndModelNames(results){
		def idSet = [] as Set
		def modelSet = [] as Set
		results.each{r->
			idSet<<r.sampleID
			modelSet<<r.modelName
		}
		System.err.println idSet
		System.err.println modelSet
		return([idSet,modelSet])
	}
	
	def runTSne(results){
		def idSet,modelSet
		(idSet,modelSet) = getIDAndModelNames(results)
		def t = new DoubleTable(modelSet as ArrayList,idSet as ArrayList)
		results.each{r->
			//System.err.println "${r.modelName}\t${r.sampleID}\t"
			//System.err.println "pr: ${r.prForValues[0]}"
			t.set(r.modelName,r.sampleID,r.prForValues[0])
		}
		
		double perplexity = 20.0
		int initial_dims = t.cols();
		def X = t.toArray()
		
		System.err.println("Running TSne...");
		TSne tsne = new FastTSne();
		double [][] Y = tsne.tsne(X, 2, initial_dims, perplexity);
		System.err.println("done.")
		 
		System.err.println Y				
	}
	
/**

//int initial_dims = 25;
//double perplexity = 20.0;
double perplexity = args[2] as double;

t = new DoubleTable(args[0])
int initial_dims = t.cols();

//t.matrix.normalize()
X = t.toArray()

println "Center, scale, and log..."
//X = MatrixOps.log(X, true);
//X = MatrixOps.centerAndScale(X);
println "done."

TSne tsne = new FastTSne();
double [][] Y = tsne.tsne(X, 2, initial_dims, perplexity);

new File(args[1]).withWriter{w->
	Y.each{
		w.writeLine it.join("\t")
	}
}

*/
	
}

