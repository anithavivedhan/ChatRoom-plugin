package jenkins.plugins.ChatRooms;

import hudson.Util;
import hudson.model.*;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.AffectedFile;
import hudson.scm.ChangeLogSet.Entry;
import java.io.IOException;
import org.apache.commons.lang.StringUtils;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import static jenkins.plugins.ChatRooms.StandardChatService.setup;

@SuppressWarnings("rawtypes")
public class ActiveNotifier implements EventsNotifier {

    private static final Logger logger = Logger.getLogger(ChatListener.class.getName());

    ChatNotifier notifier;

    public ActiveNotifier(ChatNotifier notifier) {
        super();
        this.notifier = notifier;
         try{
        setup();
        } catch (IOException e) {
        }
        
    }

    private ChatService getChatRoom(AbstractBuild r) {
        AbstractProject<?, ?> project = r.getProject();
        String projectRoom = Util.fixEmpty(project.getProperty(ChatNotifier.ChatJobProperty.class).getRoom());
        return notifier.newChatService(projectRoom);
    }

    public void deleted(AbstractBuild r) {
    }

    public void started(AbstractBuild build) {
        logger.info("started build");
        String changes = getChanges(build);
        CauseAction cause = build.getAction(CauseAction.class);

        if (changes != null) {
            notifyStart(build, changes);
        } else if (cause != null) {
            MessageBuilder message = new MessageBuilder(notifier, build);
            message.append(cause.getShortDescription());
            notifyStart(build, message.appendOpenLink().toString());
        } else {
            notifyStart(build, getBuildStatusMessage(build));
        }
    }

    private void notifyStart(AbstractBuild build, String message) {
        logger.info("notifyStart");
        getChatRoom(build).publish(message, "green");
    }

    public void finalized(AbstractBuild r) {
    }

    public void completed(AbstractBuild r) {
         logger.info("completed1");
        AbstractProject<?, ?> project = r.getProject();
        ChatNotifier.ChatJobProperty jobProperty = project.getProperty(ChatNotifier.ChatJobProperty.class);
        Result result = r.getResult();
        AbstractBuild<?, ?> previousBuild = project.getLastBuild().getPreviousBuild();
        Result previousResult = (previousBuild != null) ? previousBuild.getResult() : Result.SUCCESS;
        if ((result == Result.ABORTED && jobProperty.getNotifyAborted())
                || (result == Result.FAILURE && jobProperty.getNotifyFailure())
                || (result == Result.NOT_BUILT && jobProperty.getNotifyNotBuilt())
                || (result == Result.SUCCESS && previousResult == Result.FAILURE && jobProperty.getNotifyBackToNormal())
                || (result == Result.SUCCESS && jobProperty.getNotifySuccess())
                || (result == Result.UNSTABLE && jobProperty.getNotifyUnstable())) {
            getChatRoom(r).publish(getBuildStatusMessage(r), getBuildColor(r));
        }
        logger.info("completed2");
    }

    String getChanges(AbstractBuild r) {
        if (!r.hasChangeSetComputed()) {
            logger.info("No change set computed...");
            return null;
        }
        ChangeLogSet changeSet = r.getChangeSet();
        List<Entry> entries = new LinkedList<Entry>();
        Set<AffectedFile> files = new HashSet<AffectedFile>();
        for (Object o : changeSet.getItems()) {
            Entry entry = (Entry) o;
            logger.info("Entry " + o);
            entries.add(entry);
            files.addAll(entry.getAffectedFiles());
        }
        if (entries.isEmpty()) {
            logger.info("Empty change...");
            return null;
        }
        Set<String> authors = new HashSet<String>();
        for (Entry entry : entries) {
            authors.add(entry.getAuthor().getDisplayName());
        }
        MessageBuilder message = new MessageBuilder(notifier, r);
        message.append("Started by changes from ");
        message.append(StringUtils.join(authors, ", "));
        message.append(" (");
        message.append(files.size());
        message.append(" file(s) changed)");
        return message.appendOpenLink().toString();
    }

    static String getBuildColor(AbstractBuild r) {
          try{
        setup();
        } catch (IOException e) {
        }
        Result result = r.getResult();
        if (result == Result.SUCCESS) {
            return "green";
        } else if (result == Result.FAILURE) {
            return "red";
        } else {
            return "yellow";
        }
    }

    String getBuildStatusMessage(AbstractBuild r) {
        MessageBuilder message = new MessageBuilder(notifier, r);
        message.appendStatusMessage();
        message.appendDuration();
        return message.appendOpenLink().toString();
    }

    public static class MessageBuilder {
        private StringBuffer message;
        private ChatNotifier notifier;
        private AbstractBuild build;

        public MessageBuilder(ChatNotifier notifier, AbstractBuild build) {
            this.notifier = notifier;
            this.message = new StringBuffer();
            this.build = build;
            startMessage();
              try{
        setup();
        } catch (IOException e) {
        }
        }

        public MessageBuilder appendStatusMessage() {
            message.append(getStatusMessage(build));
            return this;
        }

        static String getStatusMessage(AbstractBuild r) {
            logger.info("getStatusMessage");
            if (r.isBuilding()) {
                return "Starting...";
            }
            Result result = r.getResult();
            Run previousBuild = r.getProject().getLastBuild().getPreviousBuild();
            Result previousResult = (previousBuild != null) ? previousBuild.getResult() : Result.SUCCESS;
            if (result == Result.SUCCESS && previousResult == Result.FAILURE) return "Back to normal";
            if (result == Result.SUCCESS) return "Success";
            if (result == Result.FAILURE) return "<b>FAILURE</b>";
            if (result == Result.ABORTED) return "ABORTED";
            if (result == Result.NOT_BUILT) return "Not built";
            if (result == Result.UNSTABLE) return "Unstable";
            return "Unknown";
        }

        public MessageBuilder append(String string) {
            message.append(string);
            return this;
        }

        public MessageBuilder append(Object string) {
            message.append(string.toString());
            return this;
        }

        private MessageBuilder startMessage() {
            message.append(build.getProject().getDisplayName());
            message.append(" - ");
            message.append(build.getDisplayName());
            message.append(" ");
            return this;
        }

        public MessageBuilder appendOpenLink() {
            String url = notifier.getBuildServerUrl() + build.getUrl();
            message.append(" (<a href='").append(url).append("'>Open</a>)");
            return this;
        }

        public MessageBuilder appendDuration() {
            message.append(" after ");
            message.append(build.getDurationString());
            return this;
        }

        public String toString() {
            return message.toString();
        }
    }
}
