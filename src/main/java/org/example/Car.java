package org.example;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Car implements Runnable
{
    private static final AtomicInteger atomicWinnersCount;
    private static final List<String> winnersLists;
    private static final Lock lock;
    private static CyclicBarrier startBarrier;
    private static CountDownLatch endLatch;
    private static int carsCount;

    static
    {
        carsCount = 0;
        lock = new ReentrantLock();
        atomicWinnersCount = new AtomicInteger(0);
        winnersLists = new CopyOnWriteArrayList<>();
    }

    private final String name;
    private final Race race;
    private final int speed;

    public Car(Race race, int speed)
    {
        this.race = race;
        this.speed = speed;
        this.name = "Участник #" + ++carsCount;
    }

    public static void setStartBarrier(CyclicBarrier startBarrier)
    {
        Car.startBarrier = startBarrier;
    }

    public static void setEndLatch(CountDownLatch endLatch)
    {
        Car.endLatch = endLatch;
    }

    public static void showWinners()
    {
        for (int i = 0; i < winnersLists.size() && i < 3; i++)
        {
            System.out.println(winnersLists.get(i) + " победил. Место - " + (i+1));
        }
    }

    public String getName()
    {
        return name;
    }

    public int getSpeed()
    {
        return speed;
    }

    @Override
    public void run()
    {
        try
        {
            System.out.println(this.name + " готовится");
            Thread.sleep(500 + (int) (Math.random() * 800));
            System.out.println(this.name + " готов");
            startBarrier.await();
        }
        catch (InterruptedException | BrokenBarrierException e)
        {
            throw new RuntimeException(e);
        }

        for (int i = 0; i < race.getStages().size(); i++)
        {
            race.getStages().get(i).go(this);
            if (i < race.getStages().size() - 1)
            {
                Main.outLock.unlock();
            }
        }

        lock.lock();

        int winnerNumber = atomicWinnersCount.incrementAndGet();
        if (winnerNumber <= 3)
        {
            winnersLists.add(this.name);
            System.out.println(this.name + " победил. Место - " + winnerNumber);
        }

        lock.unlock();

        endLatch.countDown();
        Main.outLock.unlock();
    }
}