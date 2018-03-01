package amazed.solver;

import amazed.maze.Maze;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * <code>ForkJoinSolver</code> implements a solver for
 * <code>Maze</code> objects using a fork/join multi-thread
 * depth-first search.
 * <p>
 * Instances of <code>ForkJoinSolver</code> should be run by a
 * <code>ForkJoinPool</code> object.
 */


public class ForkJoinSolver
    extends SequentialSolver
    implements Runnable
{
    /**
     * Creates a solver that searches in <code>maze</code> from the
     * start node to a goal.
     *
     * @param maze   the maze to be searched
     */
    public ForkJoinSolver(Maze maze)
    {
        super(maze);
        ForkJoinSearch(maze);
    }
    
    public List<Integer> ForkJoinSearch(Maze maze){
        return ForkJoinSearch(maze, new HashMap<>());
    }

    private List<Integer> ForkJoinSearch(Maze maze, Map<Integer, Integer> predecessor){
        int player = maze.newPlayer(start);
        int current = start;        
        
            // If we stand on goal
            if(maze.hasGoal(current)){
                return pathFromTo(start, current);
            }
            Integer[] options = whereToGo(maze.neighbors(current), visited);
            switch(options.length){
                case 0:
                    return null;
                case 1:
                    maze.move(player, options[0]);
                    visited.add(current);
                    predecessor.put(options[0], current);
                    current = options[0];
                default:
                    maze.move(player, options[0]);
                    visited.add(current);
                    predecessor.put(options[0], current);
                    for (int node: options){
                        //TODO hur gör man x antal Threads??
                    }
            }

        return null;
    }

    /**
     * Takes a set of nodes and returns those not yet visited
     */
    private Integer[] whereToGo(Set<Integer> neighbors, Set<Integer> visited){
        neighbors.removeAll(visited);
        Integer[] a = new Integer[0]; //'a' will be overwritten by an array of correct size if needed
        neighbors.toArray(a);
        return a;
    }

    /**
     * Creates a solver that searches in <code>maze</code> from the
     * start node to a goal, forking after a given number of visited
     * nodes.
     *
     * @param maze        the maze to be searched
     * @param forkAfter   the number of steps (visited nodes) after
     *                    which a parallel task is forked; if
     *                    <code>forkAfter &lt;= 0</code> the solver never
     *                    forks new tasks
     */
    public ForkJoinSolver(Maze maze, int forkAfter)
    {
        this(maze);
        this.forkAfter = forkAfter;
    }

    /**
     * Searches for and returns the path, as a list of node
     * identifiers, that goes from the start node to a goal node in
     * the maze. If such a path cannot be found (because there are no
     * goals, or all goals are unreacheable), the method returns
     * <code>null</code>.
     *
     * @return   the list of node identifiers from the start node to a
     *           goal node in the maze; <code>null</code> if such a path cannot
     *           be found.
     */
    @Override
    public List<Integer> compute()
    {
        return parallelSearch();
    }

    private List<Integer> parallelSearch()
    {
        return null;
    }
}
