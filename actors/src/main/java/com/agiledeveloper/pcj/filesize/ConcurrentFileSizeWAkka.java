package com.agiledeveloper.pcj.filesize;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.japi.Creator;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

class RequestAFile {}

class FileSize {
    public final long size;
    public FileSize(final long size) {
        this.size = size;
    }
}

class FileToProcess {
    public final String fileName;
    public FileToProcess(final String fileName) {
        this.fileName = fileName;
    }
}

class FileProcessorActor extends UntypedActor {

    private final ActorRef sizeCollectorActor;

    public FileProcessorActor(final ActorRef sizeCollectorActor) {
        this.sizeCollectorActor = sizeCollectorActor;
    }

    public static Props props(final ActorRef sizeCollectorActor) {
        return Props.create(new Creator<FileProcessorActor>() {
            private static final long serialVersionUID = 1L;

            @Override
            public FileProcessorActor create() throws Exception {
                return new FileProcessorActor(sizeCollectorActor);
            }
        });
    }

    @Override
    public void preStart() {
        registerToGetFile();
    }

    private void registerToGetFile() {
        sizeCollectorActor.tell(new RequestAFile(), getSelf());
    }


    @Override
    public void onReceive(final Object message) throws Exception {
        FileToProcess fileToProcess = (FileToProcess) message;
        final File file = new File(fileToProcess.fileName);
        long size = 0L;
        if(file.isFile()) {
            size = file.length();
            // the line below should be printed only when file (not dir) was passed as a command line argument
            System.out.println(getSelf() + " received a file to process " + file);
        } else {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    if (child.isFile()) {
                        size += child.length();
                    } else {
                        sizeCollectorActor.tell(new FileToProcess(child.getPath()), getSelf());
                    }
                }
            }
        }
        sizeCollectorActor.tell(new FileSize(size), getSelf());
        registerToGetFile();
    }


}

class SizeCollectorActor extends UntypedActor {
    private final ArrayDeque<String> toProcessFileNames = new ArrayDeque<>(1024);
    private final ArrayDeque<ActorRef> idleFileProcessors =
            new ArrayDeque<>(100);
    private long pendingNumberOfFilesToVisit = 0L;
    private long totalSize = 0L;
    private long start = System.nanoTime();
    private int maxSizeOfToProcessFileName = 0;


    public static Props props() {
        return Props.create(new Creator<SizeCollectorActor>() {
            private static final long serialVersionUID = 1L;

            @Override
            public SizeCollectorActor create() throws Exception {
                return new SizeCollectorActor();
            }
        });
    }

    private void sendAFileToProcess() {
        if(!toProcessFileNames.isEmpty() && !idleFileProcessors.isEmpty()) {
            idleFileProcessors.poll().tell(
                    new FileToProcess(toProcessFileNames.poll()), getSelf());
        }
    }



    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof  RequestAFile) {
            idleFileProcessors.offer(getSender());
            sendAFileToProcess();
        }

        if (message instanceof FileToProcess) {
            toProcessFileNames.offer(((FileToProcess)(message)).fileName);
            final int size = toProcessFileNames.size();
            if (size > maxSizeOfToProcessFileName) {
                maxSizeOfToProcessFileName = size;
            }
            pendingNumberOfFilesToVisit++;
            sendAFileToProcess();
        }

        if (message instanceof FileSize) {
            totalSize += ((FileSize)(message)).size;
            pendingNumberOfFilesToVisit--;

            if(pendingNumberOfFilesToVisit == 0) {
                long end = System.nanoTime();
                System.out.println("Total size is " + totalSize);
                System.out.println("Time taken is " + (end - start)/1.0e9);
                System.out.println("MaxSize = " + this.maxSizeOfToProcessFileName);
                getContext().system().shutdown();
            }
        }



    }
}

/**
 * @author mpakhomov
 * @since: 7/3/2015
 */
public class ConcurrentFileSizeWAkka {
    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.create("MySystem");
        final ActorRef sizeCollectorActor = system.actorOf(SizeCollectorActor.props(), "SizeCollectorActor");

        sizeCollectorActor.tell(new FileToProcess(args[0]), ActorRef.noSender());

        for(int i = 0; i < 100; i++) {
            system.actorOf(FileProcessorActor.props(sizeCollectorActor), "actor" + i);
//            Actors.actorOf(new UntypedActorFactory() {
//                public UntypedActor create() {
//                    return new FileProcessor(sizeCollector);
//                }
//            }).start();
        }

    }
}
