elaine
======

## .dat files

`1.dat` and `2.dat` are sample data files. Columns one and two list the
emissions and states, respectively. I left the code for generating the data
in train.m.

## train.m

Run `train.m` with sample data (1.dat and 2.dat)...

```bash
$ matlab -nodisplay -r "train('.',2,6);exit"
```

`train.m` will loop through every `.dat` in `.` and load the observations
into two cell arrays for emissions and states.

The model is randomly initialized. Initializing the model from ML estimates
can be done by simply commenting out the TRGUESS and EMITGUESS blocks and
reading the data from files.

The guesses are passed to `hmmtrain` with the corpus of observed emission
sequences to train the model.

By default, hidden Markov model functions begin in state 1, so we change
the initial state distribution to a uniform distribution over states.
ML estimates may be used by commenting out the line and substituting
the estimates.

## Hmm.java

```bash
$ javac -cp matlabcontrol-4.1.0.jar:. Hmm.java
$ java -cp matlabcontrol-4.1.0.jar:. Hmm [LEN]
```

## References
* [HMM](http://www.mathworks.com/help/stats/hidden-markov-models-hmm.html)
* [matlabcontrol](https://code.google.com/p/matlabcontrol/wiki/Walkthrough)
