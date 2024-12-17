package com.nortal.activedirectoryrestapi;

import org.postgresql.PGNotification;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Constants {
    public static final String NEW_USER = "New-ADUser";
    public static final String GET_USER = "Get-ADUser";
    public static final String REMOVE_USER = "Remove-ADUser";
    public static final String SET_USER = "Set-ADUser";
    public static final String NEW_GROUP = "New-ADGroup";
    public static final String GET_GROUP = "Get-ADGroup";
    public static final String REMOVE_GROUP = "Remove-ADGroup";
    public static final String SET_GROUP = "Set-ADGroup";
    public static final String GET_GROUP_MEMBER = "Get-ADGroupMember";
    public static final String ADD_GROUP_MEMBER = "Add-ADGroupMember";
    public static final String REMOVE_GROUP_MEMBER = "Remove-ADGroupMember";
    public static final String ENABLE_ACCOUNT = "Enable-ADAccount";
    public static final String DISABLE_ACCOUNT = "Disable-ADAccount";
    public static final String SET_ACCOUNT_PASSWORD = "Set-ADAccountPassword";
}
