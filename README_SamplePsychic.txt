Sample Psychic
=================

SamplePsychic is on github, and requires two other github 
projects: 

https://github.com/jdurbin/SamplePsychic
https://github.com/jdurbin/grapnel
https://github.com/jdurbin/VaadinHighChartsAPI

SamplePsychic is written in a combination of groovy and java.  

It uses the Vaadin (https://vaadin.com) java web application framework. 

I have done all SamplePsychic development in Eclipse (http://www.eclipse.org) 
because Eclipse has:
	- A Vaadin plug-in that simplifies using Vaadin
	- A groovy plug-in that simplifies using groovy in the web-app. 
	- Support for compiling and running the webapp under jetty in one step. 
	- Under the hood Eclipse uses Maven for the build.   
		- Could set up a command-line Maven-only build. 

The Eclipse Vaadin and groovy plug-ins are required.   
	
Sample Psychic uses my personal fork of VaadinHighChartsAPI:

https://github.com/jdurbin/VaadinHighChartsAPI

This fork just adds APIs for more features of HighCharts (http://www.highcharts.com)
javascript charting library. 

======================

Some quirky things:

MOST dependencies for SamplePsychic are handled through maven, which downloads
the dependencies from the maven repository.  

A small set of dependencies aren't (or weren't when I started) in the maven 
repository.  These I copy directly into the right directory in the SamplePsychic
project: 

grapnel.jar
highchartsapi-2.2.2d.jar
colt-1.2.0.jar
groovy-2.4.7.jar
jMEF.jar
jMEFTools.jar
tsne-0.0.2.jar
weka-3-8-9.jar

grapnel is compiled from my source
highchartsapi is compiled from my fork of highchartsapi

All of these files are copied into two places: 

workspace/samplepsychic/target/samplepsychic-1.0-SNAPSHOT/WEB-INF/lib/
workspace/samplepsychic/src/main/webapp/WEB-INF/lib/

One makes the library available to *compilation*, the other location
makes sure it is bundled with the WAR file for the webapp.  

The right way to do this would be to track these files in a local 
maven repository or something, but I haven't been motivated enough to 
do that so far. 


