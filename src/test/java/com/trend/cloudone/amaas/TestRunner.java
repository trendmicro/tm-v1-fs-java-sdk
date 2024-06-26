
package com.trend.cloudone.amaas;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;


public final class TestRunner {
   private TestRunner() {
   }

   public static void main(final String[] args) {
      Result result = JUnitCore.runClasses(TestSuite.class);

      for (Failure failure : result.getFailures()) {
         System.out.println(failure.toString());
      }

      System.out.println(result.wasSuccessful());
   }
}
