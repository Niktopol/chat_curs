"use client"

import { fetchUserSession } from "@/store/thunks/userSessionThunk";
import { useRouter } from "next/navigation";
import { useEffect } from "react";
import { useDispatch } from "react-redux";

export default function FetchUserSession({ mustHaveSession, redirect }) {
    const dispatch = useDispatch();
    const router = useRouter();

    useEffect(() => {
        if (mustHaveSession){
            dispatch(fetchUserSession())
            .unwrap()
            .catch(() => {
                router.push(redirect);
            });
        } else {
            dispatch(fetchUserSession())
            .unwrap()
            .then(() => {
                router.push(redirect);
            }).catch(() => {});
        }
        
    }, [dispatch, router]);

    return null;
}