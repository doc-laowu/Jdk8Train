package com.neusoft.java8;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Title: streams
 * @ProjectName MyJdbcSink
 * @Description: TODO
 * @Author yisheng.wu
 * @Date 2019/8/3011:15
 */
public class streams {

    public static void main(String[] args) throws IOException {

        final Collection<Streams.Task> tasks = Arrays.asList(
                new Streams.Task( Streams.Status.OPEN, 5 ),
                new Streams.Task( Streams.Status.OPEN, 13 ),
                new Streams.Task( Streams.Status.CLOSED, 8 )
        );

        // Calculate total points of all active tasks using sum()
        final long totalPointsOfOpenTasks = tasks
                .stream()
                // 设置是否并行运行
                .parallel()
                .filter( task -> task.getStatus() == Streams.Status.OPEN )
                .mapToInt( Streams.Task::getPoints )
                .sum();

        System.out.println( "Total points: " + totalPointsOfOpenTasks );

        // Calculate total points of all tasks
        final double totalPoints = tasks
                .stream()
                .parallel()
                .map( task -> task.getPoints() ) // or map( Task::getPoints )
                .reduce( 0, Integer::sum );

        System.out.println( "Total points (all tasks): " + totalPoints );

        // Group tasks by their status
        final Map<Streams.Status, List<Streams.Task>> map = tasks
                .stream()
                .collect( Collectors.groupingBy( Streams.Task::getStatus ) );
        System.out.println( map );


        // Calculate the weight of each tasks (as percent of total points)
        final Collection< String > result = tasks
                .stream()                                        // Stream< String >
                .mapToInt( Streams.Task::getPoints )                     // IntStream
                .asLongStream()                                  // LongStream
                .mapToDouble( points -> points / totalPoints )   // DoubleStream
                .boxed()                                         // Stream< Double >
                .mapToLong( weigth -> ( long )( weigth * 100 ) ) // LongStream
                .mapToObj( percentage -> percentage + "%" )      // Stream< String>
                .collect( Collectors.toList() );                 // List< String >

        System.out.println( result );

        // read file content per line by stream api
        final Path path = new File( "C:\\Users\\gaosen\\Desktop\\zhike之前杨毅的接口.txt" ).toPath();
        try( Stream< String > lines = Files.lines( path, StandardCharsets.UTF_8 ) ) {
            lines.onClose( () -> System.out.println("Done!") ).forEach( System.out::println );
        }

    }

    public static class Streams  {
        private enum Status {
            OPEN, CLOSED
        };

        private static final class Task {
            private final Status status;
            private final Integer points;

            Task( final Status status, final Integer points ) {
                this.status = status;
                this.points = points;
            }

            public Integer getPoints() {
                return points;
            }

            public Status getStatus() {
                return status;
            }

            @Override
            public String toString() {
                return String.format( "[%s, %d]", status, points );
            }
        }
    }

}
