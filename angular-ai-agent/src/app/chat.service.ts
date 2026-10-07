import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ChatService {

  private apiUrl = 'http://localhost:8080/api/chat';

  constructor(private http: HttpClient) {}

  sendMessage(
    conversationId: string,
    message: string
  ): Observable<any> {

    return this.http.post<any>(
      this.apiUrl,
      {
        conversationId,
        message
      }
    );
  }

  uploadFile(
    file: File,
    conversationId: string,
    message: string
  ): Observable<string> {

    const formData = new FormData();

    formData.append('file', file);
    formData.append('conversationId', conversationId);
    formData.append('message', message);

    return this.http.post(
      `${this.apiUrl}/upload`,
      formData,
      {
        responseType: 'text'
      }
    );
  }

  getHistory(
    conversationId: string
  ): Observable<any[]> {

    return this.http.get<any[]>(
      `${this.apiUrl}/history/${conversationId}`
    );
  }

  getConversations(): Observable<any[]> {

    return this.http.get<any[]>(
      `${this.apiUrl}/history`
    );
  }

  deleteChat(
    conversationId: string
  ): Observable<string> {

    return this.http.delete(
      `${this.apiUrl}/history/${conversationId}`,
      {
        responseType: 'text'
      }
    );
  }
}