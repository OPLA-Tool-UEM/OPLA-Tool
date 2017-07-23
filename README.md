# OPLA-Tool

## Description
Desenvolvimento da OPLA-Memetic.

You need download all projects before to build the OPLA-Memetic.

This project was created from the project 
```sh

https://github.com/JoaoChoma/marcelo-opla.git
```

## Requirements
Before to compile the code, you need to install the following softwares on your PC:
- Java Development Kit (Version >= 6)
- Git - http://git-scm.com
- Maven - http://maven.apache.org

## How to Build
This section show the step-by-step that you should follow to build the OPLA-Tool. 

- Create a directory to build OPLA-Tool:
```sh
mkdir opla-tool
```
- Access the folder:
```sh
cd opla-tool
```
- Download all projects:
```sh

git clone https://github.com/OPLA-Tool-UEM/opla-tool.git
```

- Compile
```sh
mvn clean install
```
- Open OPLA-Tool:
```sh
java -jar modules/opla-tool/target/opla-tool-0.0.1-jar-with-dependencies.jar
```





