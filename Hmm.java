import java.util.*;
import matlabcontrol.*;
import matlabcontrol.extensions.*;

public class Hmm {
  private final static MatlabProxyFactory factory = new MatlabProxyFactory();
  private double[][] t_hat;
  private double[][] e_hat;

  public void train(String pathToData, int numStates, int numEmissions) throws
    MatlabConnectionException, MatlabInvocationException {
    MatlabProxy proxy = factory.getProxy();
    MatlabTypeConverter converter = new MatlabTypeConverter(proxy);

    proxy.setVariable("pathToData", pathToData);
    proxy.setVariable("numStates", numStates);
    proxy.setVariable("numEmissions", numEmissions);

    // train.m is in PATH...
    proxy.eval("[t_hat,e_hat] = train(pathToData,numStates,numEmissions)");

    t_hat = converter.getNumericArray("t_hat").getRealArray2D();
    e_hat = converter.getNumericArray("e_hat").getRealArray2D();

    proxy.disconnect();
  }

  public int[] generate(int k) throws
    MatlabConnectionException, MatlabInvocationException {
    MatlabProxy proxy = factory.getProxy();
    MatlabTypeConverter converter = new MatlabTypeConverter(proxy);

    proxy.setVariable("k", k);
    converter.setNumericArray("t_hat", new MatlabNumericArray(t_hat, null));
    converter.setNumericArray("e_hat", new MatlabNumericArray(e_hat, null));

    proxy.eval("rng('shuffle')");
    proxy.eval("[emissions,states] = hmmgenerate(k,t_hat,e_hat)");

    double[] states = (double[]) proxy.getVariable("states");
    int[] s = new int[states.length];
    // since we augmented TRANS to change the initial state distribution...
    for (int i = 0; i < s.length; i++)
      s[i] = (int) states[i] - 1;
    System.out.println("states..." + Arrays.toString(s));

    double[] emissions = (double[]) proxy.getVariable("emissions");
    int[] e = new int[emissions.length];
    for (int i = 0; i < e.length; i++)
      e[i] = (int) emissions[i];
    System.out.println("emissions..." + Arrays.toString(e));

    proxy.disconnect();

    return e;
  }

  public int interactive(int currentState) {
    int istate = currentState + 1; //since we augmented TRANS
    //System.out.println("istate = " + istate);

    List<Double> p = new ArrayList<Double>();
    List<Integer> c = new ArrayList<Integer>();

    //System.out.println("t_hat[0] = " + Arrays.toString(t_hat[0]));
    //System.out.println("t_hat[1] = " + Arrays.toString(t_hat[1]));
    //System.out.println("t_hat[2] = " + Arrays.toString(t_hat[2]));
    double cumsum = 0.0;
    // NOTE t_hat[istate][0] will always be zero...
    for (int j = 0; j < t_hat[0].length; j++) {
      if (t_hat[istate][j] > 0) {
        p.add(cumsum + t_hat[istate][j]);
        c.add(j);
        cumsum += t_hat[istate][j];
      }
    }
    //System.out.println("p = " + Arrays.toString(p.toArray()));
    //System.out.println("c = " + Arrays.toString(c.toArray()));

    Random random = new Random();
    double variate = random.nextDouble();
    //double variate = 0.9;
    //System.out.println("variate = " + variate);

    int fstate = 0; // how we will index into e_hat
    for (int i = 0; i < p.size(); i++) {
      //System.out.println("p.get(i) = " + p.get(i));
      fstate = c.get(i);
      //System.out.println("fstate = " + fstate);
      if (variate < p.get(i))
        break;
    }

    p.clear();
    c.clear();

    //System.out.println("getting emission...");
    //System.out.println("fstate = " + fstate);
    //System.out.println("e_hat[0] = " + Arrays.toString(e_hat[0]));
    //System.out.println("e_hat[1] = " + Arrays.toString(e_hat[1]));
    //System.out.println("e_hat[2] = " + Arrays.toString(e_hat[2]));

    cumsum = 0.0;
    for (int j = 0; j < e_hat[0].length; j++) {
      if (e_hat[fstate][j] > 0) {
        p.add(cumsum + e_hat[fstate][j]);
        c.add(j);
        cumsum += e_hat[fstate][j];
      }
    }
    //System.out.println("p = " + Arrays.toString(p.toArray()));
    //System.out.println("c = " + Arrays.toString(c.toArray()));

    variate = random.nextDouble();
    //variate = 0.99785;
    //System.out.println("variate = " + variate);

    int emission = e_hat[0].length; //row length
    for (int i = 0; i < p.size(); i++) {
      //System.out.println("p.get(i) = " + p.get(i));
      emission = c.get(i);
      //System.out.println("emission = " + emission);
      if (variate < p.get(i))
        break;
    }
    emission += 1; // since these emissions are zero-indexed
    System.out.println("interactive emission..." + emission);

    return emission;
  }

  public static void main(String[] args) throws
    MatlabConnectionException, MatlabInvocationException {
    Hmm hmm = new Hmm();
    hmm.train(".", 2, 6); // sample data have 2 states and 6 emissions

    int[] emissions = hmm.generate(Integer.parseInt(args[0]));

    int state = 0;
    int interactive = hmm.interactive(state);
  }
}
