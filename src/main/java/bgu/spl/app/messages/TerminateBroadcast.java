package bgu.spl.app.messages;

import bgu.spl.mics.Broadcast;

/**
 * This broadcast is sent by the TimeService when the services should terminate.
 * All services should subscribe to this message.
 */
public class TerminateBroadcast implements Broadcast {
}
