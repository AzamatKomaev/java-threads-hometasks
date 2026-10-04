package ru.netology.sync;

public class TransferService {

    public static final class InsufficientFundsException extends RuntimeException {
        public InsufficientFundsException(String message) {
            super(message);
        }
    }

    public void transfer(BankAccount from, BankAccount to, long amount) {
        // захват блокировок всегда в порядке возрастания id - независимо от направления перевода,
        // поэтому два потока с обратными переводами не могут захватить счета в разном порядке
        BankAccount first = from.getId() < to.getId() ? from : to;
        BankAccount second = from.getId() < to.getId() ? to : from;

        synchronized (first) {
            synchronized (second) {
                if (from.getBalance() < amount) {
                    throw new InsufficientFundsException(
                            "Недостаточно средств на счёте " + from.getId());
                }
                from.withdraw(amount);
                to.deposit(amount);
            }
        }
    }
}
