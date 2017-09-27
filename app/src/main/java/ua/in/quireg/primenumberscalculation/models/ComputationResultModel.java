package ua.in.quireg.primenumberscalculation.models;

public class ComputationResultModel {
    int threadID;
    int numbersCalculated;

    public ComputationResultModel(int threadID, int numbersCalculated) {
        this.threadID = threadID;
        this.numbersCalculated = numbersCalculated;
    }
}
