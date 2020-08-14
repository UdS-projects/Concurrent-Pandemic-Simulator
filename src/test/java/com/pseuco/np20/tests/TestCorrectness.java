package com.pseuco.np20.tests;

import com.pseuco.np20.tests.common.TestCase;

import org.junit.Test;


public class TestCorrectness {
    @Test
    public void testWeLoveNP10() {
        TestCase.getPublic("we_love_np").launchRocket(10);
    }

    @Test
    public void testWeLoveNP15() {
        TestCase.getPublic("we_love_np").launchRocket(15);
    }

//    @Test
//    public void ourTest() {
//        TestCase.getPublic("we_love_np").launchRocket(3);
//    }
}