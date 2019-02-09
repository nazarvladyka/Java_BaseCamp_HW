package com.basecamp.service.impl;

import com.basecamp.exception.InternalException;
import com.basecamp.exception.InvalidDataException;
import com.basecamp.service.ProductService;
import com.basecamp.wire.GetHandleProductIdsResponse;
import com.basecamp.wire.GetProductInfoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class ProductServiceImpl implements ProductService {

    private final ConcurrentTaskService taskService;

    public GetProductInfoResponse getProductInfo(String productId) {

        validateId(productId);

        log.info("Product id {} was successfully validated.", productId);

        return callToDbAnotherServiceETC(productId);
    }

    public GetHandleProductIdsResponse handleProducts(List<String> productIds) {
        Map<String, Future<String>> handledTasks = new HashMap<>();
        productIds.forEach(productId ->
                handledTasks.put(
                        productId,
                        taskService.handleProductIdByExecutor(productId)));

        List<String> handledIds = handledTasks.entrySet().stream().map(stringFutureEntry -> {
            try {
                return stringFutureEntry.getValue().get(3, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                log.error(stringFutureEntry.getKey() + " execution error!");
            }

            return stringFutureEntry.getKey() + " is not handled!";
        }).collect(Collectors.toList());

        return GetHandleProductIdsResponse.builder()
                .productIds(handledIds)
                .build();
    }

    public void stopProductExecutor() {
        log.warn("Calling to stop product executor...");

        taskService.stopExecutorService();

        log.info("Product executor stopped.");
    }

    private void validateId(String id) {

        if (StringUtils.isEmpty(id)) {
            // all messages could be moved to messages properties file (resources)
            String msg = "ProductId is not set.";
            log.error(msg);
            throw new InvalidDataException(msg);
        }

        try {
            Integer.valueOf(id);
        } catch (NumberFormatException e) {
            String msg = String.format("ProductId %s is not a number.", id);
            log.error(msg);
            throw new InvalidDataException(msg);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new InternalException(e.getMessage());
        }
    }

    private GetProductInfoResponse callToDbAnotherServiceETC(String productId) {
        return GetProductInfoResponse.builder()
                .id(productId)
                .name("ProductName")
                .status("ProductStatus")
                .build();
    }

    @Override
    public void bugsRace() throws InterruptedException, ExecutionException {
        int amountOfBugs = 10;
        int distance = 100;
        ArrayList<Integer> bugSteps = new ArrayList<>(amountOfBugs);
        ArrayList<Future> futures = new ArrayList<>(amountOfBugs);
        ExecutorService executorService = Executors.newFixedThreadPool(amountOfBugs);
        //Fill array by "0" values
        for (int i = 0; i < amountOfBugs; i++) {
            bugSteps.add(0);
        }

        printDecoration("Start", amountOfBugs);
        while(Collections.max(bugSteps) < distance) {
            for (int i = 0; i < amountOfBugs; i++) {
                Future future = executorService.submit(new Bug());
                futures.add(future);
            }

            TimeUnit.MILLISECONDS.sleep(500);

            for (int i = 0; i < amountOfBugs; i++) {
                int previousValue = bugSteps.get(i);
                bugSteps.set(i, previousValue + (Integer) futures.get(i).get());
                System.out.print(bugSteps.get(i) + "\t\t");
            }
            System.out.print("\n");
            futures.clear();
        }
        executorService.shutdown();

        printDecoration("Finish", amountOfBugs);
        for (int i = 0; i < bugSteps.indexOf(Collections.max(bugSteps)); i++) {
            System.out.print("\t\t");
        }
        System.out.println("(" + Collections.max(bugSteps) + ")");
        System.out.println("That guy is the winner. Congrats!!!");
    }

    private void printDecoration(String msg, int amountOfBugs) {
        msg = msg.toUpperCase();
        for(int i = 0; i < amountOfBugs * 8; i++) {
            System.out.print("-");
        }
        System.out.println();
        for(int i = 0; i < amountOfBugs; i++) {
            System.out.print("\t");
        }
        System.out.println(msg);
        for(int i = 0; i < amountOfBugs * 8; i++) {
            System.out.print("-");
        }
        System.out.println();
    }

    class Bug implements Callable<Integer> {
        @Override
        public Integer call() {
            int maxStepsForBug = 10;
            return new Random().nextInt(maxStepsForBug);
        }
    }
}
