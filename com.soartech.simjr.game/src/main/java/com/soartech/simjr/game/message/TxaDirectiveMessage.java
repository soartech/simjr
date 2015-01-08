package com.soartech.simjr.game.message;

import com.google.protobuf.InvalidProtocolBufferException;
import com.soartech.switchboard.message.ExtendableProtoBufMessage;
import com.soartech.txa.proto.TxaDirective;

public class TxaDirectiveMessage extends ExtendableProtoBufMessage
{
    public TxaDirectiveMessage() {super(null);}
    public TxaDirectiveMessage(TxaDirective.TxaDirectiveMessage msg) {super(msg);}
    public TxaDirectiveMessage(String str) throws InvalidProtocolBufferException {super(TxaDirective.TxaDirectiveMessage.parseFrom(str.getBytes()));}
    @Override protected String getProtoClass() { return TxaDirective.TxaDirectiveMessage.class.getSimpleName(); }
    public TxaDirective.TxaDirectiveMessage get() {return (TxaDirective.TxaDirectiveMessage) data;}
}
