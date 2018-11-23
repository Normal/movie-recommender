## Running locally

 - Build project:
    
        sbt assembly
        
 
 - Run backend:
 
        spark-submit --master "local[*]" target/scala-2.11/als-spark.jar
        
 - Query examples:
 
        GET localhost:8080/user/random
        GET localhost:8080/item/random
 
 - Run frontend (for mac users):
 
        docker run -p 8888:8888 jupyter/pyspark-notebook
        
    - Then copy link from console output into browser and import **ui.ipynb**
    
    - Execute all notebook commands. 
    
    - Please be patient - it is downloading movie images from the internet - so can take a while.