Assisted-Living
===============

Meal Plan Generator using AI Techniques (Genetic Algorithm) 

This Netbeans web project is based on JSF with Primefaces library. For resolving the dependencies Maven is used. It was test successfully on Glassfish 3.1.2.2.

Libraries used:
commons-fileupload-1.3.jar
commons-io-2.4.jar
javaee-web-api-6.0.jar
opencsv-2.1.jar
primefaces-3.5.jar
JDK 1.7
javaee-endorsed-api-6.0.jar

Structure

In 'Web Pages' or the folder 'src/main/webapp' all the JSF Pages are located. They are responsible for the user interface.
In 'Source Packages' or the folder 'src/main/java' all the Java Classes are located.
The package nz.ac.wintec.bean contains all controllers, which are the interface to the JSF Pages.
The package nz.ac.wintec.dataimport is responsible for importing new data.
The package nz.ac.wintec.entity holds all entity classes, which contain the main information, such as Course or Meal.
The package nz.ac.wintec.genetic includes all Classes necessary for the genetic algorithm. This is the main package in terms of program logic.
The package nz.ac.wintec.service includes helper classes for tasks, which are needed multiple times and are not assignable to a specific class.

Usage
The Application provides an import function for courses, ingredients and restrictions as well as tables to view them. However, the main part of the software is the meal plan generator. It generates the fittest (cheapest) meal plan for the time frame specified in the settings. Besides, the maximum number of generations, the size of a population, the maximum time, the crossover and mutation probability and the decision about dismissing old generations (no statistics) can be changed. To begin the generation the evolution should be resetted, then it can be run. During the generation the progress of generating, time and, if applicable, the progress of freeing memory can be viewed. Furthermore, it is possible to interrupt the generation. After this has finished the results (fittest meal plan) and statistics can be viewed.
