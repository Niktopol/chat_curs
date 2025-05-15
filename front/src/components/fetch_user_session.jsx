"use client"

import { setChat } from "@/store/chatSelectedSlice";
import { fetchUserSession } from "@/store/thunks/userSessionThunk";
import { useRouter } from "next/navigation";
import { useEffect } from "react";
import { useDispatch } from "react-redux";

export default function FetchUserSession({ mustHaveSession, redirect }) {
    const dispatch = useDispatch();
    const router = useRouter();

    useEffect(() => {
        dispatch(setChat(
            {
                id: null,
                username: "",
                name: "",
                private: null,
                new: null,
                image: "/default_user.svg"
            }));
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
            })
            .catch(() => {});
        }
        
    }, [dispatch, router]);

    return null;
}