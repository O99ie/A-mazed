package amazed.solver;

import amazed.maze.Maze;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Iterator;

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
{
    // begin: The tile ID of this threads starting position
    private Integer begin;
    // visited, predecessor: shared versions of their super-namesakes
    static Set<Integer> visited; // Public?
    static Map<Integer, Integer> predecessor;
    // result: a variable where we save the result
    static List<Integer> result = new ArrayList();

    /**
     * Creates a solver that searches in <code>maze</code> from the
     * start node to a goal.
     *
     * @param maze   the maze to be searched
     */
    public ForkJoinSolver(Maze maze)
    {
        super(maze);
        synchronized(this){
            begin = start;
            visited = super.visited;
            predecessor = super.predecessor;
            ForkJoinPool.commonPool().invoke(this);
        }
    }

    public ForkJoinSolver(Integer n, Set<Integer> v, Map<Integer, Integer> p,  Maze maze)
    {
        super(maze);
        synchronized(this){
            begin = n;
            visited = v;
            predecessor = p;
        }
    }

    
    private List<Integer> parallelSearch(){
        Integer current = begin;        
        int player = maze.newPlayer(current);
        while(true){
            // If we stand on goal
            if(maze.hasGoal(current)){
                synchronized(this){
                    super.predecessor = predecessor;
                    result = pathFromTo(start, current);
                    return result;
                }
            }
            // options: a set of not-yet visited neighbors
            Set<Integer> options = whereToGo(maze.neighbors(current), visited);
            Iterator it = options.iterator();
            // next: the next element in the set 'options'
            Integer next;

            switch(options.size()){
                case 0:
                    return null;
                case 1:
                    synchronized(this){
                        next = (Integer) it.next();
                        maze.move(player, next);
                        predecessor.put(next, current);
                        visited.add(next);
                        current = next;
                    }
                    break;
                default:
                    // Nodes: a list of threads spawned by this thread
                    List<ForkJoinSolver> nodes = new ArrayList();

                    synchronized(this){
                        while (it.hasNext()){
                            next = (Integer) it.next();
                            predecessor.put(next, current);
                            if(it.hasNext()){
                                visited.add(next);
                                ForkJoinSolver subSearch = new ForkJoinSolver(next, visited, predecessor, maze);
                                subSearch.fork();
                                nodes.add(subSearch);
                            }else{
                                maze.move(player, next);
                                visited.add(next);
                                current = next;
                            }
                            
                        }
                    }
                    for(ForkJoinSolver node : nodes){
                        node.join();
                    }
                    break;
            }
        }
    }

    /**
     * Takes a set of nodes and returns those not yet visited
     */
    private Set<Integer> whereToGo(Set<Integer> neighbors, Set<Integer> visited){
        neighbors.removeAll(visited);
        return neighbors;
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
    public List<Integer> compute(){
        parallelSearch();
        return result;
    }
}
