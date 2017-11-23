package durbin.samplepsychic

import at.downdrown.vaadinaddons.highchartsapi.HighChart;

import at.downdrown.vaadinaddons.highchartsapi.HighChartFactory;
import at.downdrown.vaadinaddons.highchartsapi.exceptions.HighChartsException;
import at.downdrown.vaadinaddons.highchartsapi.model.ChartConfiguration;
import at.downdrown.vaadinaddons.highchartsapi.model.ChartType;
import at.downdrown.vaadinaddons.highchartsapi.model.data.HighChartsData;
import at.downdrown.vaadinaddons.highchartsapi.model.data.base.DoubleData;
import at.downdrown.vaadinaddons.highchartsapi.model.plotoptions.ColumnChartPlotOptions;
import at.downdrown.vaadinaddons.highchartsapi.model.series.ColumnChartSeries;
import at.downdrown.vaadinaddons.highchartsapi.model.data.base.DoubleDoubleData;

import com.vaadin.server.Sizeable.Unit;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Panel;

import hep.aida.ref.Histogram1D;

/***
 * A HighCharts column chart showing a histogram of values. 
 * 
 * @author james
 *
 */
class HistogramChart {		
	
	HighChart chart;
	def height=100;
	def width=150;
	
	/***
	 * Create a column chart of the given data
	 * @return
	 */
	def HistogramChart(){
		
	}		
	
	/***
	 * 		
	 * @param data
	 * @return
	 */
	def createChart(data,mark){
		
		def (binheights,bincenters) = getBins(data)
		
		ChartConfiguration chartConfig = new ChartConfiguration();
		chartConfig.setTitle("");
		chartConfig.setChartType(ChartType.COLUMN);
		chartConfig.setLegendEnabled(false);
		chartConfig.setExportingEnabled(false); // Turns off menu for saving (button too large for small plots)

		def xAxis = chartConfig.getxAxis()
		xAxis.labelStyle = """
		style: {			
			fontSize:'8px'
		}
		"""
		
		xAxis.plotLines = """
			plotLines: [{
          		dashStyle: 'Solid',
			    color: 'red', // Color value
			    value: $mark, // Value of where the line will appear
			    width: 2 // Width of the line    
  			}]
		"""
		
		xAxis.setTickInterval(0.25);
		xAxis.max = 1.0
		
		ColumnChartPlotOptions plotOptions = new ColumnChartPlotOptions();
		plotOptions.setDataLabelsEnabled(false);		
		chartConfig.setPlotOptions(plotOptions);
	
		List<HighChartsData> columnValues = new ArrayList<>();
		binheights.eachWithIndex{h,i->
			def center = bincenters[i]
			columnValues.add(new DoubleDoubleData(center,h)); // x,y
			//columnValues.add(new DoubleData(v));
		}
		ColumnChartSeries series = new ColumnChartSeries("",columnValues);
		chartConfig.getSeriesList().add(series);
		
		//def javascript = chartConfig.getHighChartValue()
		//System.err.println(javascript)
		
		try {
			chart = HighChartFactory.renderChart(chartConfig);
			chart.setHeight(height, Unit.PIXELS);
			chart.setWidth(width, Unit.PIXELS);
			return(chart)
		} catch (HighChartsException e) {
			e.printStackTrace();			
			return(null)
		}
	}
	
	/**
	 * Converts a list of values into a set of bins for a histogram.
	 * @param data list of data to be plotted.
	 * @return list (pair) of bin heights and bin centers
	 */
	def getBins(data){
		int numbins = 40
		double min = data.min()
		double max = data.max()
		def h = new Histogram1D("Title",numbins,min,max)
		data.each{d->
			h.fill(d)
		}
		def binheights = []
		def bincenters = []
		def xaxis = h.xAxis()
		(0..<numbins).each{i->
			def c = xaxis.binCentre(i)
			c = c.round(2)
			def height = h.binEntries(i)
			binheights << height
			bincenters << c
		}
		return([binheights,bincenters])
	}	
}
