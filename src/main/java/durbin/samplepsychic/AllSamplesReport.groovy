package durbin.samplepsychic
import java.awt.Image
import java.awt.image.BufferedImage
import java.util.ArrayList;
import java.util.List;

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

import com.vaadin.data.Property.ValueChangeListener
import com.vaadin.server.FileDownloader
import com.vaadin.server.FileResource
import com.vaadin.server.Resource
import com.vaadin.server.StreamResource
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.shared.ui.MarginInfo
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button
import com.vaadin.ui.Component
import com.vaadin.ui.FormLayout
import com.vaadin.ui.GridLayout
import com.vaadin.ui.HorizontalLayout
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Alignment.*;

import grapnel.weka.*;
import weka.core.*;
import weka.filters.*;
import swiftml.*;

import hep.aida.ref.Histogram1D;

/**
 * All 
 *
 * @author james
 */
class AllSamplesReport {

	ClassifierCompendium cc;
	SamplePsychicUI app;
	VerticalLayout mainLayout;
	VerticalLayout resultListLayout = new VerticalLayout()
	def summaryCSVPath;
	def cutoffValue = 0.99;

	def globalCounter = 0
	def classifiersApplied = false;
	
	Button downloadButton;
	FileDownloader fd;
	

	def AllSamplesReport(SamplePsychicUI app){
		this.app = app;
		//cc = app.mam.cc
		cc = app.compendium
	}
	
	VerticalLayout buildLayout(){
		VerticalLayout footerLayout = new VerticalLayout(new Label("FOOTER"));

		// The main scrollable content put into a layout
		buildListOfResults(resultListLayout,cutoffValue);
				
		// XXX: place the center layout into a panel, which allows scrollbars
		Panel contentPanel = new Panel(resultListLayout);
		contentPanel.setSizeFull();

		// XXX: add the panel instead of the layout
		HorizontalLayout headerLayout = buildTitleBar();
		Component buttonBar = buildButtonBar();
		@SuppressWarnings("deprecation")
		//Label horizontalLine = new Label("<hr />",Label.CONTENT_XHTML);
		
		mainLayout = new VerticalLayout(buttonBar,headerLayout,contentPanel, footerLayout);
		//mainLayout = new VerticalLayout(buttonBar,horizontalLine,headerLayout,contentPanel, footerLayout);
		mainLayout.setSizeFull();
		mainLayout.setExpandRatio(contentPanel, 1f);
		return(mainLayout);
	}

	HorizontalLayout buildTitleBar(){
		HorizontalLayout titleBar = new HorizontalLayout();
		//titleBar.addStyleName("outlined");
		titleBar.addComponent(new Label("Model"));
		titleBar.addComponent(new Label("CallValue"));
		titleBar.addComponent(new Label("Confidence"));
		titleBar.addComponent(new Label("Background"));
		titleBar.setWidth("100%");		
		titleBar.setMargin(new MarginInfo(false,false,false,true)); // top right bottom left
		return(titleBar);
	}

	public Component buildButtonBar(){
		HorizontalLayout hlayout = new HorizontalLayout();
		FormLayout fl = new FormLayout();
		fl.setSpacing(false);
		fl.setMargin(false);
		
//		fl.setStyleName("padding-top-4");		
		//hlayout.addComponent(new Label("  "));
						
		TextField cutoffValueField = new TextField("Score cutoff:","");
		cutoffValueField.setStyleName("v-textfield-narrow");
		cutoffValueField.setValue("$cutoffValue");
		cutoffValueField.setMaxLength(5);
		cutoffValueField.setColumns(6);
		cutoffValueField.addValueChangeListener({ event ->
			cutoffValue = cutoffValueField.getValue() as double
			buildListOfResults(resultListLayout,cutoffValue)

			// Attach the button to the temporary file containing the summary.
			System.err.println "Set FileDownloader resource: $summaryCSVPath"
			Resource res = new FileResource(new File(summaryCSVPath));
			fd.setFileDownloadResource(res);			
		} as ValueChangeListener)
						
		//cutoffValue.setWidth(100.0f,Unit.PERCENTAGE);
		fl.addComponent(cutoffValueField);
		hlayout.addComponent(fl);
			
		downloadButton = new Button("Download CSV");
		downloadButton.setStyleName("v-button-narrow");
		
		hlayout.addComponent(downloadButton);
		
		// Attach the button to the temporary file containing the summary.
		Resource res = new FileResource(new File(summaryCSVPath));
		fd = new FileDownloader(res);
		fd.extend(downloadButton);
				
		hlayout.setWidth("100%");
		// top right bottom left
		hlayout.setMargin(new MarginInfo(false, true, false, true));
		return(hlayout);
	}
	
	
	/***
	 * Create a panel with a list of results for each sample.  The contents of this
	 * panel will be scrollable.
	 * @return
	 */
	def buildListOfResults(contentLayout,cutoff){
		// In case we are rebuilding the layout...
		contentLayout.removeAllComponents();
				
		//VerticalLayout contentLayout = new VerticalLayout();
		contentLayout.setMargin(new MarginInfo(true,true,true,true)); // top right bottom left
		contentLayout.setSpacing(true);
				
		def results = app.results;
		// Get the best results and build a result by sample list.
		def bestResults = cc.getBestResults(results,cutoff)
		System.err.println("Total Results: ${results.size()} Best results: "+bestResults.size())
		def sample2ResultList = cc.getResultsBySample(bestResults)
		

		createReportCardCSV(sample2ResultList,cutoff)
				
		sample2ResultList.each{sampleID,rlist->
			
			// A panel for each sample...
			def samplePanel = new Panel(sampleID);
			samplePanel.addStyleName('v-hover-panel');
			samplePanel.addStyleName('v-panel-caption-color2');
			
			// A bunch of results for each sample...
			def multiResultsVLayout = new VerticalLayout()
			multiResultsVLayout.setMargin(true)
			rlist.each{r->
				def singleResultHLayout = createSingleResultLayout(r,sampleID)
				multiResultsVLayout.addComponent(singleResultHLayout)
			}
			samplePanel.setContent(multiResultsVLayout);
			contentLayout.addComponent(samplePanel);
		}
		return(contentLayout)
	}
	
	
	def createReportCardCSV(sample2Result,cutoff){
		// Create a file for the results.
		
		//File temp = File.createTempFile("temp",".csv")
		File temp = File.createTempFile("${app.fileNameRoot}_${cutoff}_",".csv")
		temp.deleteOnExit()		
		
		temp.withWriter{w->
			System.err.println "Sample ID\tClassifier\tClassifier Call\tClassifier Probability\tBackground Confidence"
			w.println "Sample ID,Classifier,Classifier Call,Classifier Probability,Background Confidence"
			sample2Result.each{sampleID,rList->
				rList.each{r->
					def mname = r.modelName
					def model = cc.modelName2Model[mname]

					def (callValue,idx) = r.callAndIdx()
					
					// Only interested in results where best call is 0
					if (idx != 0) return;
					//def idx = 0; // Always take the perspective of the 0 value. 
					//def callValue = r.classValues[idx] 
					
					def bnm = model.bnm
					def dynamicbin = bnm.nullDistribution[idx]
					def values = dynamicbin.elements()
					def elements = values.elements() as ArrayList
					def pr = r.prForValues[idx] // probability.
					pr = pr.round(3)
					def nullConf = model.bnm.getSignificance(pr,idx)
					nullConf = nullConf.round(3)
					def pr0 = (float) r.prForValues[0]
					System.err.println "$sampleID\t${r.modelName}\t${callValue}\t${pr}\t${nullConf}"		
					w.println "$sampleID,${r.modelName},${callValue},${pr},${nullConf}"		
				}
			}
		}
		System.err.println "NEW TEMP FILE: ${temp.getAbsolutePath()}"
		summaryCSVPath = temp.getAbsolutePath()
	}
	

	/***
	 * Generate the contents for a single line of results for a single sample.
	 * @param r
	 * @param sampleID
	 * @return
	 */
	def createSingleResultLayout(r,sampleID){
		def mname = r.modelName
		def model = cc.modelName2Model[mname]

		// Layout for one result in a sample's panel.
		def resultLayout = new HorizontalLayout();
		resultLayout.setSizeFull();

		//def (callValue,idx) = r.callAndIdx()
		def idx = 0; // Always take the perspective of the 0 value. 
		def callValue = r.classValues[idx] 
				
		def bnm = model.bnm
		def dynamicbin = bnm.nullDistribution[idx]
		def values = dynamicbin.elements()
		def elements = values.elements() as ArrayList
		def pr = r.prForValues[idx] // probability.
		pr = pr.round(3)
		def nullConf = model.bnm.getSignificance(pr,idx)
		nullConf = nullConf.round(3)
		def pr0 = (float) r.prForValues[0]

		resultLayout.addComponent(new Label("<small>${r.modelName}</small>",ContentMode.HTML));
		resultLayout.addComponent(new Label("<small>${callValue}</small>",ContentMode.HTML));
		resultLayout.addComponent(new Label("<small>${nullConf}</small>",ContentMode.HTML));

		def hc = new HistogramChart()
		def chart = hc.createChart(elements,pr)
		resultLayout.addComponent(chart)
		return(resultLayout);
	}

}