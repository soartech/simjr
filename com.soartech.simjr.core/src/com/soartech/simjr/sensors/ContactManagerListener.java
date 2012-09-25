package com.soartech.simjr.sensors;

public interface ContactManagerListener
{
    /**
     * Called whenever a new contact is created.
     * 
     * @param contact Newly created contact
     */
    public void createdContact(Contact contact);
    
    /**
     * Called whenever an existing contact is destroyed.
     * 
     * @param contact Contact that was destroyed/removed.
     */
    public void destroyedContact(Contact contact);

    /**
     * Called whenever the state of a contact has changed.
     * 
     * @param contact The contact whose state has changed
     * @param oldState The previous state of the contact
     */
    public void contactStateChanged(Contact contact, ContactState oldState);
    
}
