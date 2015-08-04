import java.util.Arrays;
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

  public double[] generate(int k) throws
    MatlabConnectionException, MatlabInvocationException {

    MatlabProxy proxy = factory.getProxy();
    MatlabTypeConverter converter = new MatlabTypeConverter(proxy);

    proxy.setVariable("k", k);
    converter.setNumericArray("t_hat", new MatlabNumericArray(t_hat, null));
    converter.setNumericArray("e_hat", new MatlabNumericArray(e_hat, null));

    proxy.eval("rng('shuffle')");
    proxy.eval("[emissions,states] = hmmgenerate(k,t_hat,e_hat)");

    double[] states = (double[]) proxy.getVariable("states");
    // since we augmented TRANS to change the initial state distribution...
    for (int i = 0; i < states.length; i++) states[i] = states[i] - 1;
    System.out.println("states..." + Arrays.toString(states));

    double[] emissions = (double[]) proxy.getVariable("emissions");
    System.out.println("emissions..." + Arrays.toString(emissions));

    proxy.disconnect();

    return emissions;
  }

  public static void main(String[] args) throws
    MatlabConnectionException, MatlabInvocationException {

    Hmm hmm = new Hmm();
    hmm.train(".", 2, 6); // sample data have 2 states and 6 emissions
    double[] emissions = hmm.generate(Integer.parseInt(args[0]));
  }
}
