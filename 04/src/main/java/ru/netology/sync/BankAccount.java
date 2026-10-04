package ru.netology.sync;

public class BankAccount {

    private final int id;
    private long balance;

    public BankAccount(int id, long balance) {
        this.id = id;
        this.balance = balance;
    }

    public int getId() {
        return id;
    }

    public synchronized long getBalance() {
        return balance;
    }

    void deposit(long amount) {
        balance += amount;
    }

    void withdraw(long amount) {
        balance -= amount;
    }
}
