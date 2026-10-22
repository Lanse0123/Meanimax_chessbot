package lanse.worldwarchess.shared.core.piece.ai.attacks.projectile;

public class ProjectileAttack {

    //TODO - this will be the projectile superclass.
    // it has a direction, a velocity, a size, power, range, and some other factors.
    // it will store what board it is on. On each tick, it will see if its colliding with a piece that isnt the piece
    // that shot it. If so, do collision logic. Otherwise, step forward. Check if it is still on the board. If not,
    // find what board it is on. If no boards, currentBoard = null and it can skip collision checks.
    // Once it finds a new board, it can use that and check collisions again.
    // Once it runs out of range, erase it.

}
