# Instructions to Run the Benchmark

There are two parts to the benchmark code. They are as follows.

1. **DataGenerator**: This generates data for the benchmarking. It takes as input the following.
   	- The port that it listens on for incoming connections.
   	- The text file for input. The '\n'-delimited lines in the file are considered input data records.
   	- The byte rate it sends the data (serialized lines) to any client that connects to it.
   
2. **Benchmark** - This consumes the generated data and after every batch interval (say, 1 second), prints the number of records received in the batch interval. It takes as input the following.
	- The number streams it will create. This is the number of parallel data generators it is going to connect to. Typically this is the number of workers on which the benchmark is being run on (or 1 if it is being run on a local machine).
	- The host and port of the data generator it is going to connect to. 
	- The batch interval in milliseconds. 1000 is typical.

## Running on a Local Machine

Git clone the repository on to your local machine.

	git clone 

On terminal 1, run the DataGenerator. This uses the file 100-bytes-lines.txt where each line is 100 byte long, thus creating records of 100 bytes.

	> sbt/sbt "run-main DataGenerator 9999 100-bytes-lines.txt 1000000 10000000"

The output should be something like this.
	
	[info] Set current project to benchmark-app
	[info] Running DataGenerator 9999 100-bytes-lines.txt 10000000 100000000
	Listening on port 9999

	
On terminal 2, run the Benchmark application.

	> sbt/sbt "run-main Benchmark 1 localhost 9999 1000" 2>&1 | grep -i "jobscheduler\|record"
	
The output should be something like this. Pay attention to the *Total delay*, it should stay stable. If it keeps increasing, then the system is not able to process the data as fast as it is being received, so its workload is unsustainable.

	14/08/13 20:42:50 INFO scheduler.JobScheduler: Started JobScheduler
	14/08/13 20:42:51 INFO scheduler.JobScheduler: Added jobs for time 1407987771000 ms
	14/08/13 20:42:51 INFO scheduler.JobScheduler: Starting job streaming job 1407987771000 ms.0 from job set of time 1407987771000 ms
	Received 463075 records
	14/08/13 20:42:51 INFO scheduler.JobScheduler: Finished job streaming job 1407987771000 ms.0 from job set of time 1407987771000 ms
	14/08/13 20:42:51 INFO scheduler.JobScheduler: Total delay: 0.991 s for time 1407987771000 ms (execution: 0.947 s)
	14/08/13 20:42:52 INFO scheduler.JobScheduler: Added jobs for time 1407987772000 ms
	14/08/13 20:42:52 INFO scheduler.JobScheduler: Starting job streaming job 1407987772000 ms.0 from job set of time 1407987772000 ms
	Received 926150 records
	14/08/13 20:42:52 INFO scheduler.JobScheduler: Finished job streaming job 1407987772000 ms.0 from job set of time 1407987772000 ms
	14/08/13 20:42:52 INFO scheduler.JobScheduler: Total delay: 0.691 s for time 1407987772000 ms (execution: 0.672 s)
	14/08/13 20:42:53 INFO scheduler.JobScheduler: Added jobs for time 1407987773000 ms
	14/08/13 20:42:53 INFO scheduler.JobScheduler: Starting job streaming job 1407987773000 ms.0 from job set of time 1407987773000 ms
	Received 926150 records


## Running on a Cluster

To run it a on a Spark cluster of *N* worker nodes (that is, machines on which Spark executors will be launched), you will have to start a DataGenerator on each of the worker nodes. Then launch the Benchmark with *N* as the number of streams, `localhost` as hostname, and `port` as the data generator's port. 
