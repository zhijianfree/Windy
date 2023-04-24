## Windy
Windy is a simple devops tool,it provides multi funtions to promise business. windy provide the pipeline web ui to help engineer to build and deploy thier codes.and it provides the feature design tool to help engineer to design how to test their code.

### Global Design
windy use web broker to support user to manage their pipelines and features. when web user run task, Master node will scan the tasks and split task to multi sub tasks, then dispatch sub task to client. when client receive the sub task , it will run it and notify task staus to master.

![整体设计-整体设计](https://user-images.githubusercontent.com/21210211/233932843-8f0374aa-0862-4f69-9bf6-7c1b32746875.png)

### Pipline

![整体设计-流水线](https://user-images.githubusercontent.com/21210211/233935517-06b86d99-107c-4dd3-9314-15c7492a119f.png)

pipeline has multi sub nodes, every node define a single task. web user can define their own pipeline. And pipeline allow you add third service to execute, you just need config your service.

#### Custom Node Define
you just need two steps to define your own pipeline node
- Trigger Task: define a post url and body parameters to request your http interface.
- Query Status: define a post url and body parameters to get task result
when pipeline get result from  "Query status", pipeline will check  whether the result is success. 

For example: <br/>
user config the node that define "Trigger Task" url("http://localhost:8070/v1/devops/client/build") to tigger build, and config "Query Status" url("http://localhost:8070/v1/devops/client/build/status") to get execute status. and from "Query Status" define, pipeline will check whether  param "status" match expect value "1" 
<img width="1405" alt="image" src="https://user-images.githubusercontent.com/21210211/233949274-bb714a11-7466-49fe-88ea-7850063f4360.png">

### Feature
windy provide web ui to define how to test code. when you write your feature, you can user http,mysql,dubbo,etc... templte to complete your feature. if you have your own template , you also can add it in windy 

For example:
when you config your feature, you can drag template from right list to  middle area. and you can config parameters.

<img width="1431" alt="image" src="https://user-images.githubusercontent.com/21210211/233954458-4a0c7972-4b34-437c-9955-604fd2402bbc.png">

### Template
you can manage templates in windy
<img width="1433" alt="image" src="https://user-images.githubusercontent.com/21210211/233955404-c05dd560-f10b-4a4c-b7d3-0beaf453f3d2.png">


