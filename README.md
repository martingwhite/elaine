elaine
======

## .dat files

1.dat and 2.dat are sample data files for the demo. Columns one and two
list the emissions and states, respectively. I left the code for
generating the data in train.m.

## train.m

You can run train.m with sample data (1.dat and 2.dat)...

```bash
$ matlab -nodisplay -r "train(2,6);exit"
```

train.m will loop through every .dat and load the observations into two cell
arrays for emissions and states.

The model is randomly initialized. Initializing the model from ML estimates
can be done by simply commenting out the TRGUESS and EMITGUESS blocks and
reading the data from files.

The guesses are passed to hmmtrain with the corpus of observed emission
sequences to train the model.

By default, hidden Markov model functions begin in state 1, so we change
the initial state distribution to a uniform distribution over states.
ML estimates may be used by commenting out the line and substituting
the estimates.

## Hmm.java

HMM will run a demo...

```bash
$ javac -cp matlabcontrol-4.1.0.jar:. Hmm.java
$ java -cp matlabcontrol-4.1.0.jar:. Hmm
```

Not sure how specifically you are looking to integrate the HMM stuff,
so I left several examples of getting and setting data in a MATLAB
session from Java.

Everything is in main but it should be easy to siphon off parts for the
interface depending on the need.

## References
* [HMM](http://www.mathworks.com/help/stats/hidden-markov-models-hmm.html)
* [hmmgenerate](http://www.mathworks.com/help/stats/hmmgenerate.html)
* [hmmtrain](http://www.mathworks.com/help/stats/hmmtrain.html)
* [matlabcontrol](https://code.google.com/p/matlabcontrol/wiki/Walkthrough)
* [matlabcontrol](http://engr.case.edu/ray_soumya/Sepia/html/javadoc/matlabcontrol/package-index.html)
