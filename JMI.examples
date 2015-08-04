import java.util.Arrays;
import matlabcontrol.*;
import matlabcontrol.extensions.*;

public class Hmm {
  public static void main(String[] args) throws
    MatlabConnectionException, MatlabInvocationException {
    /*
    MatlabProxyFactoryOptions options = new MatlabProxyFactoryOptions
      .Builder()
      ...
      .build();
    MatlabProxyFactory factory = new MatlabProxyFactory(options);
    */
    MatlabProxyFactory factory = new MatlabProxyFactory();
    MatlabProxy proxy = factory.getProxy();

    // example1...getting variables
    System.out.println("example1...");
    proxy.eval("n = 15");
    proxy.eval("T = [0.90 0.10; 0.05 0.95]");
    proxy.eval("E = [1/6 1/6 1/6 1/6 1/6 1/6; 7/12 1/12 1/12 1/12 1/12 1/12]");
    proxy.eval("[seq,states] = hmmgenerate(n,T,E)");
    double[] seq = (double[]) proxy.getVariable("seq");
    double[] states = (double[]) proxy.getVariable("states");
    System.out.println("generated sequence of emissions...");
    System.out.println(Arrays.toString(seq));
    System.out.println("generated sequence of states...");
    System.out.println(Arrays.toString(states));

    // example2...setting variables
    System.out.println("example2...");
    int n = 10;
    proxy.setVariable("n",n); // pass new sequence length to session...
    // NOTE T and E are still live...
    proxy.eval("[seq,states] = hmmgenerate(n,T,E)");
    seq = (double[]) proxy.getVariable("seq");
    states = (double[]) proxy.getVariable("states");
    System.out.println("generated sequence of emissions...");
    System.out.println(Arrays.toString(seq));
    System.out.println("generated sequence of states...");
    System.out.println(Arrays.toString(states));

    // example3...returning version of feval
    System.out.println("example3...");
    n = 5;
    // NOTE f.m is in PATH...function [seq,n] = f(n); seq = 1:n;
    Object[] retval = proxy.returningFeval("f",2,n); // return 2 thingies
    System.out.print("seq = ");
    System.out.println(Arrays.toString((double[]) retval[0]));
    System.out.print("n = ");
    System.out.println(Arrays.toString((double[]) retval[1]));

    // example4...train
    System.out.println("example4...");
    MatlabTypeConverter c = new MatlabTypeConverter(proxy);
    int num_states = 2;
    proxy.setVariable("num_states",num_states);
    int num_emissions = 6;
    proxy.setVariable("num_emissions",num_emissions);
    proxy.eval("[TRANS_HAT,EMIS_HAT] = train(num_states,num_emissions)");

    double[][] TRANS_HAT = c.getNumericArray("TRANS_HAT").getRealArray2D();
    System.out.println("TRANS_HAT...");
    for(int i = 0; i < TRANS_HAT.length; i++) {
      System.out.println(Arrays.toString(TRANS_HAT[i]));
    }

    double[][] EMIS_HAT = c.getNumericArray("EMIS_HAT").getRealArray2D();
    System.out.println("EMIS_HAT...");
    for(int i = 0; i < EMIS_HAT.length; i++) {
      System.out.println(Arrays.toString(EMIS_HAT[i]));
    }

    // example5...hmmgenerate
    System.out.println("example5...");
    proxy.setVariable("n",10); // request sequence of length n
    c.setNumericArray("T",new MatlabNumericArray(TRANS_HAT,null));
    c.setNumericArray("E",new MatlabNumericArray(EMIS_HAT,null));
    proxy.eval("[seq,states] = hmmgenerate(n,T,E)");
    seq = (double[]) proxy.getVariable("seq");
    System.out.println("sequence to use for something...");
    System.out.println(Arrays.toString(seq));

    proxy.disconnect();
  }
}
