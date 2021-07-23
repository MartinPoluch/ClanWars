package Testing;

public class SimulationMain {

    public static void main(String[] args) {
        Simulator simulator = new Simulator(Difficulty.MEDIUM, Difficulty.MEDIUM, 4);
        simulator.simulate(100000);
        simulator.printResult();
    }
}
