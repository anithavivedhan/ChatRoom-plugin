package jenkins.plugins.ChatRooms;

import hudson.model.AbstractBuild;

@SuppressWarnings("rawtypes")
public class BuildDisabledNotifier implements EventsNotifier {
    public void started(AbstractBuild r) {
    }

    public void deleted(AbstractBuild r) {
    }

    public void finalized(AbstractBuild r) {
    }

    public void completed(AbstractBuild r) {
    }
}
