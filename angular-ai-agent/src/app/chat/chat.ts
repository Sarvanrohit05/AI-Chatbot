import { Component, inject, ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ChatService } from '../chat.service';
import { AuthService } from '../auth';
import { Router } from '@angular/router';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './chat.html',
  styleUrl: './chat.css'
})
export class Chat {

  private chatService = inject(ChatService);
  private authService = inject(AuthService);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);

  conversationId = '';
  message = '';
  messages: any[] = [];
  conversations: any[] = [];
  loading = false;

  // Selected file
  selectedFile: File | null = null;

  constructor() {

    this.loadConversations();

    const selectedConversationId =
      localStorage.getItem('selectedConversationId');

    if (selectedConversationId) {

      this.conversationId = selectedConversationId;

      this.chatService
        .getHistory(selectedConversationId)
        .subscribe({
          next: (history) => {

            this.messages = history;
            this.cdr.detectChanges();

          },
          error: (error) => {

            console.error(
              'History loading error:',
              error
            );

            localStorage.removeItem(
              'selectedConversationId'
            );

            this.newChat();
          }
        });

    } else {

      this.newChat();

    }
  }

  newChat() {

    localStorage.removeItem(
      'selectedConversationId'
    );

    this.conversationId =
      'chat-' + Date.now();

    this.messages = [];
    this.message = '';
    this.selectedFile = null;
  }

  sendMessage() {

    if (this.loading) {
      return;
    }

    const userMessage =
      this.message.trim();

    /*
     * If an image is selected,
     * upload image with the question.
     */
    if (this.selectedFile) {

      const file = this.selectedFile;

      const uploadQuestion =
        userMessage || 'Describe this image';

      this.messages.push({
        role: 'USER',
        content:
          `[Image: ${file.name}] ${uploadQuestion}`
      });

      this.message = '';
      this.loading = true;

      this.chatService
        .uploadFile(
          file,
          this.conversationId,
          uploadQuestion
        )
        .subscribe({

          next: (response) => {

            this.messages.push({
              role: 'AI',
              content: response
            });

            localStorage.setItem(
              'selectedConversationId',
              this.conversationId
            );

            this.selectedFile = null;
            this.loading = false;

            this.cdr.detectChanges();

            this.loadConversations();
          },

          error: (error) => {

            console.error(
              'Image upload error:',
              error
            );

            this.messages.push({
              role: 'AI',
              content:
                'Sorry, image analysis failed.'
            });

            this.selectedFile = null;
            this.loading = false;

            this.cdr.detectChanges();
          }
        });

      return;
    }

    /*
     * Normal text chat.
     */
    if (!userMessage) {
      return;
    }

    this.messages.push({
      role: 'USER',
      content: userMessage
    });

    this.message = '';
    this.loading = true;

    this.chatService
      .sendMessage(
        this.conversationId,
        userMessage
      )
      .subscribe({

        next: (response) => {

          localStorage.setItem(
            'selectedConversationId',
            this.conversationId
          );

          this.messages.push({
            role: 'AI',
            content: response.response
          });

          this.cdr.detectChanges();

          this.loading = false;

          this.loadConversations();
        },

        error: (error) => {

          console.error(
            'Chat error:',
            error
          );

          this.messages.push({
            role: 'AI',
            content:
              'Sorry, something went wrong.'
          });

          this.cdr.detectChanges();

          this.loading = false;
        }
      });
  }

  // Select image only. Upload happens when Send is clicked.
  onFileSelected(event: Event) {

    const input =
      event.target as HTMLInputElement;

    if (!input.files || input.files.length === 0) {
      return;
    }

    this.selectedFile = input.files[0];

    console.log(
      'Selected file:',
      this.selectedFile.name
    );

    this.cdr.detectChanges();
  }

  selectConversation(
    conversation: any
  ) {

    this.conversationId =
      conversation.conversationId;

    localStorage.setItem(
      'selectedConversationId',
      this.conversationId
    );

    this.chatService
      .getHistory(
        this.conversationId
      )
      .subscribe({

        next: (history) => {

          this.messages = history;

          this.cdr.detectChanges();
        },

        error: (error) => {

          console.error(
            'History error:',
            error
          );
        }
      });
  }

  deleteChat(
    conversation: any,
    event: Event
  ) {

    event.stopPropagation();

    const conversationId =
      conversation.conversationId;

    this.chatService
      .deleteChat(conversationId)
      .subscribe({

        next: () => {

          this.conversations =
            this.conversations.filter(
              c =>
                c.conversationId !== conversationId
            );

          if (
            this.conversationId === conversationId
          ) {

            this.newChat();

          }

          this.cdr.detectChanges();
        },

        error: (error) => {

          console.error(
            'Delete chat error:',
            error
          );

        }
      });
  }

  loadConversations() {

    this.chatService
      .getConversations()
      .subscribe({

        next: (data) => {

          this.conversations = data;

          this.cdr.detectChanges();
        },

        error: (error) => {

          console.error(
            'Conversation loading error:',
            error
          );
        }
      });
  }

  logout() {

    this.authService.logout();

    localStorage.removeItem(
      'selectedConversationId'
    );

    this.router.navigate([
      '/login'
    ]);
  }
}