# EmbAssi
Source code for the paper EmbAssi: Embedding Assignment Costs for Similarity Search in Large Graph Databases, Franka Bause, Erich Schubert, Nils M. Kriege, 2021.

## Usage
The algorithms contained in this package for searching in graph databases can be executed via a command line interface. Edit the shell script *embAssi_gurobi*, to reflect your Gurobi setup and run it (or use the script *embAssi*) to see a list of all available methods and parameters.

Gurobi and a Gurobi license are needed for computation of the exact graph edit distance (only used in the comparison of the various approximations (`-cA`)).

The results are written to *results.txt*.

**Required parameters:**  
    `-d, --dataset` Name of dataset  
    `-m, --method` Method to be used  
Possible methods are:
* *CLB* (EmbAssi with clb)
* *CStar*
* *Branch*
* *SLF*
* *LinD*  

A thorough description of these methods can be found in our paper and the corresponding references.  


**Optional parameters:**  
    `-clb`, `--clb` Use CLB in a prefiltering step (Acceleration with EmbAssi)  
    `-cA`, `--compareApprox` Compare different approximations (Gurobi is needed for this)  
    `-knn`, `--knnSearch` Change type of query to knn  
    `-qn`, `--queryNumber` Number of queries (Default: 50)  
    `-qr`, `--queryRange` Maximum Range or k, depending on the query type (Default: 5)  
    `-r`, `--raw` Does the dataset have disconnected graphs, edge labels or any attributes?  


### Examples
The following command accelerates the method "CStar" using EmbAssi, making the filtering step for 50 range queries on the graphs in the dataset *MUTAG* for thresholds 1 to 5:  

`./embAssi -d MUTAG -m CStar -clb`

The following command makes the filtering step for 50 range queries on the graphs in the dataset *MUTAG* for thresholds 1 to 5, using only the method "CStar", without acceleration:  

`./embAssi -d MUTAG -m CStar`

    
## Datasets
The repository contains the dataset *MUTAG* only. Further data sets in the required format are available from the website [TUDatasets: A collection of benchmark datasets for graph classification and regression.](https://chrsmrrs.github.io/datasets/).
The dataset *Protein_Complexes* can be found [here](https://github.com/BiancaStoecker/complex-similarity-evaluation/tree/master/simulated_complexes/true_constraints) and *Chembl_27* [here](https://chembl.gitbook.io/chembl-interface-documentation/downloads).  

Please note that edge labels, as well as attributes, are currently not supported.  
Important:  
If the dataset has disconnected graphs, edge labels or any attributes, these have to be removed first (using `-r` or `--raw` will generate a dataset with maximum of one discrete label per vertex and only connected graphs (keeping the largest connected component of each graph)). A preprocessed dataset (called *dsName_p*) will be generated and used for the experiments.

## Terms and conditions
When using our code please cite our paper [EmbAssi: embedding assignment costs for similarity search in large graph databases](https://doi.org/10.1007/s10618-022-00850-3):
Bause, F., Schubert, E. & Kriege, N.M. EmbAssi: embedding assignment costs for similarity search in large graph databases. Data Min Knowl Disc (2022). https://doi.org/10.1007/s10618-022-00850-3


## References
EmbAssi uses an index provided by [ELKI](https://elki-project.github.io/) and the verification algorithm of [BSS-GED](https://github.com/Hongweihuo-Lab/BSS-GED):

* Erich Schubert and Arthur Zimek: ELKI: A large open-source library for data analysis. CoRR arXiv 1902.03616.

* Xiaoyang Chen, Hongwei Huo, Jun Huan, and Jeffrey S. Vitter. An Efficient Algorithm for Graph Edit Distance Computation. Knowledge-Based Systems, 2019, 163: 762–775


## Contact information
If you have any questions, please contact [Franka Bause](https://dm.cs.univie.ac.at/team/person/112939/).
