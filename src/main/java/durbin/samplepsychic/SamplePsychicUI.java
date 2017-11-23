package durbin.samplepsychic;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Iterator;

import javax.servlet.ServletContext;
import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.ClassResource;
import com.vaadin.server.FileResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.UI;
//import com.vaadin.client.ui.Icon;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.navigator.Navigator;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.ValoTheme;

import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vaadin.server.Page;
import com.vaadin.navigator.ViewChangeListener;

import grapnel.util.*;
import grapnel.weka.*;
import weka.core.*;


@SuppressWarnings("serial")
@Theme("mytheme")
public class SamplePsychicUI extends UI {
		
	public static final long serialVersionUID = 1L;	
	
	Button uploadButton;
	
	// for testing purposes. 
	//static String modelDir = "/Users/james/ucsc/exp/debug/small_models_TCGAbackground/";
	//static String genedescriptions = "/Users/james/exp/refseqdescript/ucsc/hugo2description.txt";
	
	public static String genedescriptions = VaadinServlet.getCurrent().getServletContext().getRealPath("/genedesc/hugo2description.txt");	
	
	// Read models from a server relative directory....
	public static ClassifierCompendium compendium = new ClassifierCompendium(
			VaadinServlet.getCurrent().getServletContext().getRealPath("/models")
	);
	
	public static String modelDir = VaadinServlet.getCurrent().getServletContext().getRealPath("/models");	
	/**
	 *  Storage of Models and other Data
	 *  
	 *  TODO: This only loads classifier Compendium on first visit to website.  
	 *  Should do it when server is started.
	 *  
	 */
	//public static ClassifierCompendium compendium = new ClassifierCompendium(modelDir);
		
	
	
	
	public Instances expressionData;	
	public ArrayList<ClassificationPlus> results = new ArrayList<ClassificationPlus>();
	public Table metaData;

	// RefSeq description of genes. 
	HashMap gene2description = GeneInfo.readGeneDescriptions(genedescriptions);
	
	// Rootname for uploaded file. 
	String fileNameRoot;
	
	String currentRunID;


	@WebServlet(value = "/*", asyncSupported = true)
	@VaadinServletConfiguration(productionMode = false, ui = SamplePsychicUI.class)
	public static class Servlet extends VaadinServlet {			 
	}

	/***
	 *  UI COMPONENTS
	 *  
	 */
    //private final TestIcon testIcon = new TestIcon(100);
	SamplePsychicMenuLayout root = new SamplePsychicMenuLayout();
	ComponentContainer viewDisplay = root.getContentContainer();
	Navigator navigator;
	private final LinkedHashMap<String, String> menuItems = new LinkedHashMap<String, String>();
	CssLayout menu = new CssLayout();
	CssLayout menuItemsLayout = new CssLayout();
	
	/***
	 * Overall Initialization
	 */
	@Override
	protected void init(VaadinRequest request) {
		
		//System.err.println("STATIC SERVLET CONTEXT:"+testPath);
		
		getPage().setTitle("UCSC Sample Psychic");
		setContent(root);
		root.setWidth("100%");
		root.addMenu(buildSidebarMenu());
		addStyleName(ValoTheme.UI_WITH_MENU);
		
		navigator = new Navigator(this, viewDisplay);
		navigator.addView("loadData", new UploadView("Load Data",this));
		SignatureSelectionView ssv = new SignatureSelectionView("Select Signatures",this);
		navigator.addView("evalModels", ssv);
		TabView resultsView = new TabView(this);
		navigator.addView("results", resultsView);
		
		// Select intial button. 
		uploadButton.click();
		uploadButton.addStyleName("selected");

		String currentState = navigator.getState();		
		// load data  when just localhost:8080
		System.err.println("DEBUG: navigator.state:"+currentState); 
		
		
		// Check and see if we are loading a previous session
		// Session is specified with URL parameter like ?runID=45HTG2 
		//String runID = ParameterUtils.getRunID();
		String runID = ParameterUtils.getRunID();		
		if (runID == null){			
			navigator.navigateTo("loadData");
		}else{			
			currentRunID = runID;
			// Verify that the saved session actually exists
			System.err.println("runID in SamplePsychicUI: "+runID);
			String resultsFileName = SessionUtils.getResultsFileNameFromToken(runID);
			System.err.println("Loading results from "+resultsFileName+"...");
			results = compendium.loadResults(resultsFileName);
			System.err.println("done. "+results.size()+" results loaded.");
			if (results != null){	
				navigator.navigateTo("results/"+runID);
			}else{
				navigator.navigateTo("loadData");
				System.err.println("ERROR: No results have been saved for session"+runID);
			}
		}							
		
        navigator.setErrorView(TabView.class);   		
        
        /*
        ServletContext sc = VaadinServlet.getCurrent().getServletContext();        
        if (compendium == null){
        	 compendium = new ClassifierCompendium();        	
        	 String modelpath = sc.getRealPath("/models");
        	 System.err.println("SERVLET CONTEXT PATH: "+modelpath); // null?? :-(
        	 compendium.readModels(modelpath);
        }
        */
        
        addNavigationChangeListener();
	}
	
	/***
	 * addNavigationChangeListener
	 */
	public void addNavigationChangeListener(){		
		// CHANGE LISTENER FOR NAVIGATOR
        navigator.addViewChangeListener(new ViewChangeListener() {
            @Override
            public boolean beforeViewChange(final ViewChangeEvent event) {
                return true;
            }
            
            /** A lot of code just to highlight the selected item... alas...*/
            @Override
            public void afterViewChange(final ViewChangeEvent event) {
                for (final Iterator<Component> it = menuItemsLayout.iterator(); it.hasNext();) {
                    it.next().removeStyleName("selected");
                }                                
                
                for (final Entry<String, String> item : menuItems.entrySet()) {                	
                    if (event.getViewName().equals(item.getKey())) {
                        for (final Iterator<Component> it = menuItemsLayout
                                .iterator(); it.hasNext();) {
                            final Component c = it.next();
                            if (c.getCaption() != null
                                    && c.getCaption().startsWith(
                                            item.getValue())) {
                                c.addStyleName("selected");
                                break;
                            }
                        }
                        break;
                    }
                }                                
                menu.removeStyleName("valo-menu-visible");                          
                
            } // afterViewChange
            
        }); 		
	}
		
	/****
	 * Build the sidebar menu. 
	 */
	CssLayout buildSidebarMenu() {   
        
        //final HorizontalLayout top = new HorizontalLayout();
        final VerticalLayout top = new VerticalLayout();
        top.setWidth("100%");
        top.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
        top.addStyleName("valo-menu-title");
        menu.addComponent(top);     
         
        // Add sample Psychic logo
        addLogo(top);
        
        menuItemsLayout.setPrimaryStyleName("valo-menuitems");
        menu.addComponent(menuItemsLayout);
       
        String tooltip="Upload expression data.";	
        uploadButton = createButton(FontAwesome.UPLOAD,"loadData","1) Upload",tooltip);
        tooltip = "Apply signatures to data.";
        createButton(FontAwesome.MAGIC,"evalModels","2) Analyze",tooltip);
        tooltip = "Explore signature report card";
        createButton(FontAwesome.BAR_CHART_O,"results","3) Explore",tooltip);                    			    
		
        return menu;
    }
	
	/**
	 * createButton
	 */
	public Button createButton(FontAwesome icon,String menuKey,String menuLabel,String tooltip){	
		menuItems.put(menuKey, menuLabel);  // Record the pair for future use. 
		
		final Button b = new Button(menuLabel, new ClickListener() {
			@Override
			public void buttonClick(final ClickEvent event) {
				if (menuKey == "results") {
					String keyPlusID = menuKey+"/"+currentRunID;
					navigator.navigateTo(keyPlusID);
				}
				else navigator.navigateTo(menuKey);
	        }
	    });           
	            
		b.setHtmlContentAllowed(true);	           
		b.setPrimaryStyleName("valo-menu-item");
		b.setIcon(icon);
		
		b.setDescription(tooltip);							
		
		menuItemsLayout.addComponent(b);
		return(b);
	}		
	
	/*****
	 * 	addLogo
	 */
	public static void addLogo(VerticalLayout top){
		// UCSC... image... Sample Psychic
        //final Label title1 = new Label("<h2><strong>UCSC</strong></h2>", ContentMode.HTML);
        //title1.setSizeUndefined();
        //top.addComponent(title1);
        
        // Find the application directory (Image as a file resource)
        String basepath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();
        String fullname = basepath +"/WEB-INF/images/psychic_med.jpg";
        FileResource resource = new FileResource(new File(fullname));
        Image image = new Image("", resource);                          
        top.addComponent(image);
                
        final Label title = new Label(
                "<h3><strong>Sample Psychic</strong></h3>", ContentMode.HTML);
        title.setSizeUndefined();
        top.addComponent(title);
        //top.setExpandRatio(title, 1);
    }	
}


