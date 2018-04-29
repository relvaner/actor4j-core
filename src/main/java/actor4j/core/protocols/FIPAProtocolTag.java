package actor4j.core.protocols;

// @see http://www.fipa.org/specs/fipa00037/SC00037J.html
public final class FIPAProtocolTag {
	public static final int OFFSET = 0;
	
	/**
	 * The action of accepting a previously submitted proposal to perform an action.
	 */
	public static final int ACCEPT_PROPOSAL   = OFFSET;
	/**
	 * The action of agreeing to perform some action, possibly in the future.
	 */
	public static final int AGREE 			  = OFFSET + 1;
	/**
	 * The action of one agent informing another agent that the first agent no longer has the intention
	 * that the second agent performs some action.
	 */
	public static final int CANCEL            = OFFSET + 2;
	/**
	 * The action of calling for proposals to perform a given action.
	 */
	public static final int CALL_FOR_PROPOSAL = OFFSET + 3;
	/**
	 * The sender informs the receiver that a given proposition is true, where the receiver is known to
	 * be uncertain about the proposition.
	 */
	public static final int CONFIRM           = OFFSET + 4;
	/**
	 * The sender informs the receiver that a given proposition is false, where the receiver is known to
	 * believe, or believe it likely that, the proposition is true.
	 */
	public static final int DISCONFIRM        = OFFSET + 5;
	/**
	 * The action of telling another agent that an action was attempted but the attempt failed.
	 */
	public static final int FAILURE           = OFFSET + 6;
	/**
	 * The sender informs the receiver that a given proposition is true.
	 */
	public static final int INFORM            = OFFSET + 7;
	/**
	 * A macro action for the agent of the action to inform the recipient whether or not a proposition is true.
	 */
	public static final int INFORM_IF         = OFFSET + 8;
	/**
	 * A macro action for sender to inform the receiver the object which corresponds to a descriptor,
	 * for example, a name.
	 */
	public static final int INFORM_REF        = OFFSET + 9;
	/**
	 * The sender of the act (for example, i) informs the receiver (for example, j) that it perceived that j
	 * performed some action, but that i did not understand what j just did. A particular common case is
	 * that i tells j that i did not understand the message that j has just sent to i.
	 */
	public static final int NOT_UNDERSTOOD    = OFFSET + 10;
	/**
	 * The sender intends that the receiver treat the embedded message as sent directly to the
	 * receiver, and wants the receiver to identify the agents denoted by the given descriptor and
	 * send the received propagate message to them.
	 */
	public static final int PROPAGATE    	  = OFFSET + 11;
	/**
	 * The action of submitting a proposal to perform a certain action, given certain preconditions.
	 */
	public static final int PROPOSE      	  = OFFSET + 12;
	/**
	 * The sender wants the receiver to select target agents denoted by a given description and to send
	 * an embedded message to them.
	 */
	public static final int PROXY      	      = OFFSET + 13;
	/**
	 * The action of asking another agent whether or not a given proposition is true.
	 */
	public static final int QUERY_IF      	  = OFFSET + 14;
	/**
	 * The action of asking another agent for the object referred to by a referential expression.
	 */
	public static final int QUERY_REF      	  = OFFSET + 15;
	/**
	 * The action of refusing to perform a given action, and explaining the reason for the refusal.
	 */
	public static final int REFUSE      	  = OFFSET + 16;
	/**
	 * The action of rejecting a proposal to perform some action during a negotiation.
	 */
	public static final int REJECT_PROPOSAL   = OFFSET + 17;
	/**
	 * The sender requests the receiver to perform some action.
	 * One important class of uses of the request act is to request the receiver to perform another
	 * communicative act.
	 */
	public static final int REQUEST           = OFFSET + 18;
	/**
	 * The sender wants the receiver to perform some action when some given proposition becomes true.
	 */
	public static final int REQUEST_WHEN      = OFFSET + 19;
	/**
	 * The sender wants the receiver to perform some action as soon as some proposition becomes
	 * true and thereafter each time the proposition becomes true again.
	 */
	public static final int REQUEST_WHENEVER  = OFFSET + 20;
	/**
	 * The act of requesting a persistent intention to notify the sender of the value of a reference, and to 
	 * notify again whenever the object identified by the reference changes.
	 */
	public static final int SUBSCRIBE         = OFFSET + 21;
}
