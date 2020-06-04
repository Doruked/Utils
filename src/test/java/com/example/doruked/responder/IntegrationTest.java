package com.example.doruked.responder;

import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class IntegrationTest {


    public void test_integration_by_checking_observer_returns_the_expected_response() {
        Observer<List<Object>, List<Object>> observer = (e) -> { //an observer that adds a new object to notified list
            e.add(new Object());
            return e;
        };
        List<Object> info = new ArrayList<>();

        try {
            @SuppressWarnings("unchecked")
            Mediator<List<Object>, List<Object>, List<Object>> mediator = (Mediator<List<Object>, List<Object>, List<Object>>) Mockito.mock(Mediator.class);
            Mockito.when(mediator.notify(info)).thenReturn(observer.process(info));

            @SuppressWarnings("unchecked")
            Context<List<Object>> context = (Context<List<Object>>) Mockito.mock(Context.class);
            Mockito.when(context.getContext()).thenReturn(info);

            int initial = info.size();
            List<Object> result = mediator.notify(context.getContext());

            //assert values
            int current = result.size();
            assertEquals(initial + 1, current);
            fail("Result(" + result + ") did not contain a value where it should have");

        } catch (InterruptedException e) {
            fail("Method should not be interrupted");
        }


    }

}