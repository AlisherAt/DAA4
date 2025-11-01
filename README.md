# DAA4
Graph Algorithms for Task Scheduling
This project implements graph algorithms for analyzing task dependencies in scheduling systems. It was developed as part of an algorithms course assignment to solve real-world scheduling problems.
What This Project Does
This Java application analyzes task dependencies using three main graph algorithms:
1.	Strongly Connected Components (SCC) - Finds groups of tasks that depend on each other in cycles
2.	Topological Sorting - Orders tasks based on their dependencies
3.	Shortest/Longest Paths - Finds optimal and critical paths through task networks
The system processes different types of task dependency graphs and generates detailed analysis reports.
Getting Started
What You'll Need
•	Java 11 or newer
•	Maven (for building)
•	IntelliJ IDEA (makes things easier)
How to Run It
1.	First, generate the test data:
bash
mvn compile exec:java -Dexec.mainClass="graph.DatasetGenerator"
2.	Process all datasets at once:
bash
mvn compile exec:java -Dexec.mainClass="graph.BatchProcessor"
3.	Or process just one dataset:
bash
mvn compile exec:java -Dexec.mainClass="graph.Main" -Dexec.args="data/small_cycle.json"
What You Get
After running the analysis, you'll get three types of reports for each dataset:
Components Report - Shows groups of interdependent tasks
Metrics Report - Provides statistics about the task network
Paths Report - Shows optimal and critical task sequences
Testing
To run the tests:
bash
mvn test
Real-World Uses
This code can help with:
1.	Planning city maintenance schedules
2.	Optimizing project task sequences
3.	Identifying critical dependencies in workflows
4.	Detecting circular dependencies that cause deadlocks
About This Project
This was created for a university algorithms course to demonstrate practical applications of graph theory in scheduling problems. The implementation focuses on clean, understandable code that solves real problems.

