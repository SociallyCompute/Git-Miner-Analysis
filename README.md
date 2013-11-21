Git-Miner-Analysis
==================
Gitminer Instructions.xlsx provides instructions for setting up Gitminer and Gremlin and running Gitminer to collect data. Gitminer creates a graph database

The groovy files connect directly to the graph database. 
1. Helpers.groovy is copied (and slightly modified) from gitminer and it is used by newcomers.groovy and coordination.groovy
2. newcomers.groovy is for the newcomers analysis
3. coordination.groovy is most of the analysis from cscw paper that got rejected :(
4. salt.out is sample output from coordination.groovy
5. coordination.R is an R script that performs statistical analysis on the output from coordination.groovy

How to run coordination.groovy (from the gitminer directory):
./gremlin.sh -e coordination.groovy > output_filename
prior to running update coordination.groovy to the correct db location and repo name by modifying these two lines (near bottom of file):
g = new Neo4jGraph("/home/kellyb/new_data/graph.db")
repos = ["saltstack/salt"]

How to run newcomers.groovy (from the gitminer directory):
./gremlin.sh -e newcomers.groovy > output_filename
prior to running update coordination.groovy to the correct db location and repo name by modifying these two lines (near bottom of file):
g = new Neo4jGraph("/home/kellyb/new_data/graph.db")
repos = ["saltstack/salt"]


Other files
1. getRepoList.php and getRepoList_lowstars.php are the scripts used to search GitHub for projects based on the number of forks and stars. Each file defines $repolistQuery which is defined based on the url of the advanced search performed on GitHub. To perform a different search, go to https://github.com/search/advanced. Enter the parameters for your search and run the search. Update the definition of $repolistQuery with the details from the url of the search results.

2. queries.grm also connects directly to the graph database and outputs the data into ~ separated files for upload into a relational database for those more comfortable working with relational databases. 

