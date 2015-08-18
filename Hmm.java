import java.io.*;
import java.lang.*;
import java.nio.file.*;
import java.util.*;
import matlabcontrol.*;
import matlabcontrol.extensions.*;

public class Hmm {
  private final static MatlabProxyFactory factory = new MatlabProxyFactory();
  private double[][] t_hat;
  private double[][] e_hat;

  public void train(String pathToData, int numStates, int numEmissions) {
    t_hat = new double[numStates+1][numStates+1];
    e_hat = new double[numStates+1][numEmissions];
    Path directory = Paths.get(pathToData);
    try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory,"*.dat")) {
      for (Path file : directoryStream) {
        System.out.println(file.toString());
        Scanner scanner = new Scanner(new File(file.toString()));
        String line = scanner.nextLine();

        // e_hat is 0-indexed but the data are 1-indexed so we subtract one...
        int firstEmission = Integer.parseInt(line.trim().split("\t")[0]) - 1;
        int firstState = Integer.parseInt(line.trim().split("\t")[1]);

        t_hat[0][firstState] += 1; // this row holds the initial state distribution
        // note that the first row of e_hat is the zero vector
        e_hat[firstState][firstEmission] += 1;

        int prevState = firstState;
        int currState;
        int currEmission;

        while (scanner.hasNextLine()) {
          line = scanner.nextLine();
          // e_hat is 0-indexed but the data are 1-indexed...
          currEmission = Integer.parseInt(line.trim().split("\t")[0]) - 1;
          currState = Integer.parseInt(line.trim().split("\t")[1]);
          t_hat[prevState][currState] += 1;
          e_hat[currState][currEmission] += 1;
          prevState = currState;
        }
        scanner.close();
      }
    } catch (IOException | DirectoryIteratorException ex) {
        ex.printStackTrace();
    }

    double sum;
    for (int i = 0; i < numStates + 1; i++) {
      // normalize rows of t_hat...
      sum = 0.0;
      for (double count : t_hat[i]) {
        sum += count;
      }
      if (sum > 0) {
        for (int j = 0; j < numStates + 1; j++) {
          t_hat[i][j] /= sum;
        }
      }
      // normalize rows of e_hat...
      sum = 0.0;
      for (double count : e_hat[i]) {
        sum += count;
      }
      if (sum > 0) {
        for (int j = 0; j < numEmissions; j++) {
          e_hat[i][j] /= sum;
        }
      }
    }
  }

  public void train2(String pathToData, int numStates, int numEmissions) throws
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
    //int istate = currentState + 1;
    int istate = currentState;
    //System.out.println("istate = " + istate);

    List<Double> p = new ArrayList<Double>();
    List<Integer> c = new ArrayList<Integer>();

    double cumsum = 0.0;
    // NOTE t_hat[istate][0] will always be zero...
    for (int j = 0; j < t_hat[0].length; j++) {
      if (t_hat[istate][j] > 0) {
        p.add(cumsum + t_hat[istate][j]);
        c.add(j);
        cumsum += t_hat[istate][j];
      }
    }

    Random random = new Random();
    double variate = random.nextDouble();

    int fstate = 0; // how we will index into e_hat
    for (int i = 0; i < p.size(); i++) {
      //System.out.println("p.get(i) = " + p.get(i));
      fstate = c.get(i);
      //System.out.println("fstate = " + fstate);
      if (variate < p.get(i))
        break;
    }

    // if fstate == 0 then it is an absorbing state...
    if (fstate == 0)
        return 0;

    p.clear();
    c.clear();

    cumsum = 0.0;
    for (int j = 0; j < e_hat[0].length; j++) {
      if (e_hat[fstate][j] > 0) {
        p.add(cumsum + e_hat[fstate][j]);
        c.add(j);
        cumsum += e_hat[fstate][j];
      }
    }

    // if cumsum == 0.0 then no emissions have been observed...
    if (cumsum == 0.0)
        return 0;

    variate = random.nextDouble();

    int emission = -1; // init...it should never return -1
    for (int i = 0; i < p.size(); i++) {
      //System.out.println("p.get(i) = " + p.get(i));
      emission = c.get(i);
      //System.out.println("emission = " + emission);
      if (variate < p.get(i))
        break;
    }
    // since these emission IDs are 0-indexed...
    emission += 1; // ...but need to return emissions that are 1-indexed
    System.out.println("interactive emission..." + emission);

    return emission;
  }

  public static void main(String[] args) throws
    MatlabConnectionException, MatlabInvocationException {
    // using data Carlos sent on 8/16/2015 7:53 PM
    int numStates = 45;
    int numEmissions = 381;

    Hmm hmm = new Hmm();
    //hmm.train(".", 2, 6); // sample data have 2 states and 6 emissions
    hmm.train(".", numStates, numEmissions); // use *.ts.dat

    // deprecated...
    //int[] emissions = hmm.generate(Integer.parseInt(args[0]));

    int emission;
    // currentState == 0 corresponds to the intial state distribution...
    for (int currentState = 0; currentState < numStates + 1; currentState++) {
        System.out.println("testing state..." + currentState);
        for (int j = 0; j < 500; j++) { // test each state 500 times
            emission = hmm.interactive(currentState);
            if (emission < 1 || emission > numEmissions) {
                System.out.println("invalid emission..." + emission);
            }
        }
    }
  }
}
