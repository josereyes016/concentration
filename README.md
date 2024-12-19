# concentration

Aeris Project Assignment
You should have received the file “concentration.timeseries.nc” with this assignment.
This file is a NetCDF file and will be the foundation of this assignment. If you did not receive it, please
contact jlefebvre@aerisllc.com.
The requirements for this assignment are as follows:
1. Publicly hosted repository of your choice (GitHub, bitbucket, etc)
2. The use of Spring Boot REST framework
3. Implementation of the following endpoints
a. /get-info, returns the NetCDF detailed information.
b. /get-data, params to include time index and z index, returns json response that
includes x, y, and concentration data.
c. /get-image, params to include time index and z index, returns png visualization of
concentration.
4. Docker container deployment
5. Project README.md


Dependencies and Libs

Using JDK version 23
Using VScode with Spring Initalizer Extension
Using Maven
Using Base Image eclipse-temurin:23, https://hub.docker.com/_/eclipse-temurin
Using non root user Spring in group Spring
Using cdm-core 5.6.0 to read netcdf file
Using jzy3d lib to plot concentrations


Major Improvements

Use dependency injection to allow usage of two services for get-info. One service will return ncdump output, second service will return verbose output with data values

Create staic webpage for user to enter desired values using input html tags

netcdf file managment is handled in a different function

Handle out of bound index expections

Minor Improvements


Usage

http://localhost:8080/get-info
http://localhost:8080/get-data?timeIndex=2&zIndex=0
http://localhost:8080/get-image?timeIndex=2&zIndex=0

