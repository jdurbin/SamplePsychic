package durbin.samplepsychic

import com.vaadin.ui.Grid
import com.vaadin.ui.Notification

class DoubleTableGrid extends Grid{
	def t
	
	def DoubleTableGrid(table){
		t = table;
		//println "Table inside of grid: ${t.numRows()}"				
	}
}
