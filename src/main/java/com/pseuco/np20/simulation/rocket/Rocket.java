package com.pseuco.np20.simulation.rocket;

import com.pseuco.np20.model.Output;
import com.pseuco.np20.model.Scenario;
import com.pseuco.np20.simulation.common.Simulation;
import com.pseuco.np20.validator.InsufficientPaddingException;
import com.pseuco.np20.validator.Validator;


/**
 * Your implementation shall go into this class.
 *
 * <p>
 * This class has to implement the <em>Simulation</em> interface.
 * </p>
 */
public class Rocket implements Simulation
{
    private final int ticksAllowed;

    /**
     * Constructs a rocket with the given parameters.
     *
     * <p>
     * You must not change the signature of this constructor.
     * </p>
     *
     * <p>
     * Throw an insufficient padding exception if and only if the padding is insufficient.
     * Hint: Depending on the parameters, some amount of padding is required even if one
     * only computes one tick concurrently. The padding is insufficient if the provided
     * padding is below this minimal required padding.
     * </p>
     *
     * @param scenario The scenario to simulate.
     * @param padding The padding to be used.
     * @param validator The validator to be called.
     */
    public Rocket(Scenario scenario, int padding, Validator validator) throws InsufficientPaddingException
    {
        ticksAllowed = 5;
    }

    @Override
    public Output getOutput() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void run() {
        throw new RuntimeException("not implemented");
    }
}