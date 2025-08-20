# Running Instructions:

The `run` command compiles and runs the code:

```
    ant run
```

If youv've made changes you can run the following command to compile the changes:

```
    ant rebuild-run
```

# Technical Overview

This program can ingest data from three different sources- A text file, MongoDB, and Kafka. To change the data source simple modify the `data.source.type` to either in the *application.properties* file to one of the three accepted types-`file`, `mongo`, or `kafka`.

There is a FieldExtractor class that will be used to convert the file data into the STTC layered format.

Once extracted from the source, the data will of a 3d matrix of type String and shape [time][layer][state].

To calculate entropy, we simple invoke the `computeWindowedEntropy(String[][][] data, int windowSize, AggregationStrategy strategy)` function. It takes the extracted data, window size, and type of concatenation (explained below). The Entropy calculation works by taking all the states at a given time instance _t_, concatenating the state values according to the strategy from the params, and adding it to a frequency counter. This counter is used to caclulate probability of state occuring and subsequently the Shannon Entropy.


We require different strategies to concatenate the state variables based on the type of entropy being calculated. There are 3 different concatenation(aggregation) strategies:
- Layer wise, (Casualty, Visual, Movement, Communication): Takes the index _i_ of the layer and concatenates all the values of that singular layer
- Combined layers, (System, Team): Takes a list of layer indices and concatenates all the values across those layers to form the state representation
- Individual Entity, (Trainee T1, T2, T3): Takes a list of indices of layers as well as the index _j_ of the specific entity who's state we're representing.  